package com.vsct.impersonator.http.record;


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class HttpProxyRequestEncoderTest {

    @Test
    public void shouldCreateRequestWithFullUriAndDefaultPort() throws Exception {
        final String host = "titi.example.org";
        final InetSocketAddress address = new InetSocketAddress(host, 80);
        final HttpProxyRequestEncoder encoder = new HttpProxyRequestEncoder(address);

        final ChannelBuffer buffer = ChannelBuffers.buffer(1024);

        final String uri = "/page";
        final HttpMessage httpMessage = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);

        encoder.encodeInitialLine(buffer, httpMessage);

        final String content = buffer.toString(StandardCharsets.US_ASCII);

        assertThat(content, startsWith("GET http://" + host + uri));
    }

    @Test
    public void shouldCreateRequestWithFullUriAndCustomPort() throws Exception {
        final String host = "titi.example.org";
        final int port = 22222;
        final InetSocketAddress address = new InetSocketAddress(host, port);
        final HttpProxyRequestEncoder encoder = new HttpProxyRequestEncoder(address);

        final ChannelBuffer buffer = ChannelBuffers.buffer(1024);

        final String uri = "/page";
        final HttpMessage httpMessage = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);

        encoder.encodeInitialLine(buffer, httpMessage);

        final String content = buffer.toString(StandardCharsets.US_ASCII);

        assertThat(content, startsWith("GET http://" + host + ":" + port + uri));
    }

}
