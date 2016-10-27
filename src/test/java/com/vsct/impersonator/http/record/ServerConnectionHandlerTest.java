package com.vsct.impersonator.http.record;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.junit.Before;
import org.junit.Test;

import com.vsct.impersonator.http.message.storage.HttpExchangeStorage;

public class ServerConnectionHandlerTest {

	private ServerConnectionHandler handler;
	private ChannelHandlerContext clientContext;
	private Channel clientConnection;
	private HttpExchangeStorage exchangeStorage;

	@Before
	public void createHandler() {
		clientContext = mock(ChannelHandlerContext.class);
		clientConnection = mock(Channel.class);
		exchangeStorage = mock(HttpExchangeStorage.class);
		ChannelFuture channelFuture = mock(ChannelFuture.class);

		when(clientConnection.write(any(HttpResponse.class))).thenReturn(channelFuture);
		handler = new ServerConnectionHandler(clientContext, clientConnection, exchangeStorage);
	}

	@Test
	public void shouldProxyResponseBackToClient() throws Exception {
		ChannelHandlerContext serverContext = mock(ChannelHandlerContext.class);
		MessageEvent event = mock(MessageEvent.class);
		HttpResponse response = mock(HttpResponse.class);

		when(event.getMessage()).thenReturn(response);

		handler.messageReceived(serverContext, event);

		verify(clientConnection).write(response);
	}

}
