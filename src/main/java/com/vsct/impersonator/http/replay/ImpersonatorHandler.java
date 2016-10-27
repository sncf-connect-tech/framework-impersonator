package com.vsct.impersonator.http.replay;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.vsct.impersonator.http.message.storage.IHttpExchangeLoader;
import com.vsct.impersonator.http.util.QueryCounter;
import com.vsct.impersonator.http.util.Sleeper;

public class ImpersonatorHandler extends SimpleChannelUpstreamHandler {
	private static final Logger LOGGER = Logger.getLogger(ImpersonatorHandler.class);

	private final IHttpExchangeLoader loader;

	private final Sleeper sleeper;

	private final QueryCounter counter;

	public ImpersonatorHandler(final IHttpExchangeLoader loader, final Sleeper sleeper, final QueryCounter counter) {
		this.loader = loader;
		this.sleeper = sleeper;
		this.counter = counter;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		counter.add();
		
		Long startTime = System.currentTimeMillis();
		
		HttpRequest request = (HttpRequest) e.getMessage();
		HttpResponse response = loader.loadResponseFor(request);

		if (response.getStatus() == HttpResponseStatus.NOT_FOUND) {
			// Si on a pas trouver la réponse c'est une erreur
			counter.error();
		}

		// wait for a time lapse in order to simulate the server side activity
		if (sleeper != null) {
			sleeper.sleep(startTime);
		}

		Channel ch = e.getChannel();
		ch.write(response).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				close(future.getChannel());
			}
		});
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
		// S'il y a une exception, on augment le compteur des erreurs.
		counter.error();
		LOGGER.error(e);
		close(e.getChannel());
	}

	private void close(final Channel channel) {
		channel.close().awaitUninterruptibly();
	}
}
