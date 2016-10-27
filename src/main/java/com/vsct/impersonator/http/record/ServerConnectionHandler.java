package com.vsct.impersonator.http.record;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.vsct.impersonator.http.message.HttpExchange;
import com.vsct.impersonator.http.message.storage.IHttpExchangeStore;

class ServerConnectionHandler extends SimpleChannelUpstreamHandler {
	private final Channel clientConnection;
	private final ChannelHandlerContext clientConnectionContext;
	private final IHttpExchangeStore exchangeStorage;
	private static final Logger LOGGER = Logger.getLogger(ClientConnectionHandler.class);

	ServerConnectionHandler(final ChannelHandlerContext context, final Channel clientConnection, final IHttpExchangeStore exchangeStorage) {
		this.clientConnectionContext = context;
		this.clientConnection = clientConnection;
		this.exchangeStorage = exchangeStorage;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext context, final MessageEvent event) throws Exception {
		HttpResponse response = (HttpResponse) event.getMessage();
		HttpRequest request = (HttpRequest) clientConnectionContext.getAttachment();
		context.setAttachment(new HttpExchange(request, response));

		clientConnection.write(response).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				close(future.getChannel());
			}
		});
	}

	@Override
	public void channelClosed(final ChannelHandlerContext context, final ChannelStateEvent event) throws Exception {
		HttpExchange exchange = (HttpExchange) context.getAttachment();
		exchangeStorage.store(exchange);
		close(clientConnection);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext context, final ExceptionEvent event) throws Exception {
		LOGGER.error("unexpected exception caught on server handler, closing connection", event.getCause());
		Channel serverConnection = event.getChannel();
		close(serverConnection);
	}

	private void close(final Channel channel) {
		channel.close().awaitUninterruptibly();
	}
}