package com.vsct.impersonator.http.replay;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.vsct.impersonator.http.message.storage.IHttpExchangeLoader;
import com.vsct.impersonator.http.util.QueryCounter;
import com.vsct.impersonator.http.util.Sleeper;

public class ImpersonatorPipelineFactory implements ChannelPipelineFactory {

	private final IHttpExchangeLoader loader;

	private final Sleeper sleeper;

	private final QueryCounter queryCounter;

	private final int maxContentLength;

	public ImpersonatorPipelineFactory(final IHttpExchangeLoader loader, final Sleeper sleeper, final QueryCounter queryCounter,
			final int maxContentLength) {
		this.loader = loader;
		this.sleeper = sleeper;
		this.queryCounter = queryCounter;
		this.maxContentLength = maxContentLength;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("chunkAggregator", new HttpChunkAggregator(maxContentLength));
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
		pipeline.addLast("proxy", new ImpersonatorHandler(loader, sleeper, queryCounter));
		return pipeline;
	}
}
