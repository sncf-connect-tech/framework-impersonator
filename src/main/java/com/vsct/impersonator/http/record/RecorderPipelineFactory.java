package com.vsct.impersonator.http.record;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.google.common.base.Optional;
import com.vsct.impersonator.http.message.storage.IHttpExchangeStore;

public class RecorderPipelineFactory implements ChannelPipelineFactory {

	private final InetSocketAddress serverAddress;
	private final Optional<InetSocketAddress> proxyAddress;
	private final IHttpExchangeStore storage;
	private final ChannelFactory clientChannelFactory;
	private final boolean sslEnable;
	private final int maxContentLength;
	private final String user;
	private final String password;

	public RecorderPipelineFactory(final ChannelFactory clientChannelFactory, final IHttpExchangeStore storage,
			final InetSocketAddress serverAddress, final boolean sslEnable, final Optional<InetSocketAddress> proxyAddress,
			final int maxContentLength) {
		this.storage = storage;
		this.serverAddress = serverAddress;
		this.clientChannelFactory = clientChannelFactory;
		this.sslEnable = sslEnable;
		this.proxyAddress = proxyAddress;
		this.maxContentLength = maxContentLength;
		this.user = null;
		this.password = null;
	}

	public RecorderPipelineFactory(final ChannelFactory clientChannelFactory, final IHttpExchangeStore storage,
			final InetSocketAddress serverAddress, final boolean sslEnable, final Optional<InetSocketAddress> proxyAddress,
			final int maxContentLength, final String user, final String password) {
		this.storage = storage;
		this.serverAddress = serverAddress;
		this.clientChannelFactory = clientChannelFactory;
		this.sslEnable = sslEnable;
		this.proxyAddress = proxyAddress;
		this.maxContentLength = maxContentLength;
		this.user = user;
		this.password = password;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = org.jboss.netty.channel.Channels.pipeline();
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("chunkAggregator", new HttpChunkAggregator(maxContentLength));
		pipeline.addLast("deflater", new HttpContentDecompressor());
		pipeline.addLast("proxy", new ClientConnectionHandler(clientChannelFactory, storage, serverAddress, sslEnable, proxyAddress,
				maxContentLength, user, password));
		pipeline.addLast("encoder", new HttpResponseEncoder());

		return pipeline;
	}

}
