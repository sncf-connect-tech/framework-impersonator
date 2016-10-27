package com.vsct.impersonator.http.record;

import java.net.InetSocketAddress;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.base64.Base64;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.CharsetUtil;

import com.google.common.base.Optional;
import com.vsct.impersonator.http.message.storage.IHttpExchangeStore;

class ClientConnectionHandler extends SimpleChannelUpstreamHandler {

	private static final Logger LOGGER = Logger.getLogger(ClientConnectionHandler.class);

	private final InetSocketAddress serverAddress;
	private final Optional<InetSocketAddress> proxyAddress;

	private final IHttpExchangeStore exchangeStorage;
	private volatile Channel serverConnection;
	private final ChannelFactory clientChannelFactory;
	private final boolean sslEnable;
	private final int maxContentLength;
	private final String user;
	private final String password;

	public ClientConnectionHandler(final ChannelFactory clientChannelFactory, final IHttpExchangeStore exchangeStorage,
			final InetSocketAddress serverAddress, final boolean sslEnable, final Optional<InetSocketAddress> proxyAddress,
			final int maxContentLength, final String user, final String password) {
		if (proxyAddress.isPresent() && sslEnable) {
			throw new IllegalArgumentException("Proxy mode can't work with SSL connections");
		}

		this.serverAddress = serverAddress;
		this.exchangeStorage = exchangeStorage;
		this.clientChannelFactory = clientChannelFactory;
		this.sslEnable = sslEnable;
		this.proxyAddress = proxyAddress;
		this.maxContentLength = maxContentLength;
		this.user = user;
		this.password = password;
	}

	@Override
	public void channelOpen(final ChannelHandlerContext context, final ChannelStateEvent event) throws Exception {
		// Suspend incoming traffic until connected to the remote host.
		final Channel clientConnection = event.getChannel();
		clientConnection.setReadable(false);

		final ClientBootstrap httpClient = new ClientBootstrap(clientChannelFactory);
		setupProxyConnection(context, httpClient.getPipeline(), clientConnection);

		// Le serveur cible est le proxy, s'il est configuré
		final ChannelFuture futureConnection;
		if (proxyAddress.isPresent()) {
			futureConnection = httpClient.connect(proxyAddress.get());
		} else {
			futureConnection = httpClient.connect(serverAddress);
		}

		serverConnection = futureConnection.getChannel();
		futureConnection.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture futureServerConnection) throws Exception {
				if (futureServerConnection.isSuccess()) {
					clientConnection.setReadable(true);
				} else {
					clientConnection.close();
				}
			}
		});
	}

	private void setupProxyConnection(final ChannelHandlerContext context, final ChannelPipeline pipeline, final Channel channel) {
		if (sslEnable) {
			final SSLEngine engine = createSSLEngine();
			// Add SSL handler first to encrypt and decrypt everything.
			SslHandler sslHandler = new SslHandler(engine);
			pipeline.addLast("ssl", sslHandler);
		}

		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("chunkAggregator", new HttpChunkAggregator(maxContentLength));
		pipeline.addLast("deflater", new HttpContentDecompressor());
		pipeline.addLast("handler", new ServerConnectionHandler(context, channel, exchangeStorage));

		// La requête HTTP est encodée différement, en fonction de l'utilisation d'un proxy ou pas
		final HttpMessageEncoder httpMessageEncoder;
		if (proxyAddress.isPresent()) {
			httpMessageEncoder = new HttpProxyRequestEncoder(serverAddress);
		} else {
			httpMessageEncoder = new HttpRequestEncoder();
		}
		pipeline.addLast("encoder", httpMessageEncoder);
	}

	private SSLEngine createSSLEngine() {
		final SSLEngine engine = getClientContext().createSSLEngine();

		engine.setUseClientMode(true);
		engine.setNeedClientAuth(false);
		engine.setEnableSessionCreation(true);

		return engine;
	}

	private SSLContext getClientContext() {
		try {
			final SSLContext clientContext = SSLContext.getInstance("SSL");
			clientContext.init(null, new TrustManager[] { new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
				}

				@Override
				public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
				}
			} }, null);
			return clientContext;
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize the client-side SSLContext", e);
		}
	}

	@Override
	public void messageReceived(final ChannelHandlerContext context, final MessageEvent event) throws Exception {
		HttpRequest request = (HttpRequest) event.getMessage();
		context.setAttachment(request);
		request.setHeader(HttpHeaders.Names.HOST, serverAddress.getHostString());

		// methode pour passer en parametre le login/password en mode http header
		if (StringUtils.isNotEmpty(user) && StringUtils.isNotEmpty(password)) {
			String authString = user + ":" + password;
			ChannelBuffer authChannelBuffer = ChannelBuffers.copiedBuffer(authString, CharsetUtil.UTF_8);
			ChannelBuffer encodedAuthChannelBuffer = Base64.encode(authChannelBuffer);

			request.setHeader(HttpHeaders.Names.AUTHORIZATION, "Basic " + encodedAuthChannelBuffer.toString(CharsetUtil.UTF_8));
		}
		serverConnection.write(request);
	}

	@Override
	public void channelClosed(final ChannelHandlerContext context, final ChannelStateEvent event) throws Exception {
		close(serverConnection);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext context, final ExceptionEvent event) throws Exception {
		LOGGER.error("unexpected exception caught on client handler, closing connection", event.getCause());
		Channel clientConnection = event.getChannel();
		close(clientConnection);
	}

	private void close(final Channel channel) {
		if (channel != null && channel.isOpen() && channel.isConnected()) {
			channel.write(ChannelBuffers.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}
}
