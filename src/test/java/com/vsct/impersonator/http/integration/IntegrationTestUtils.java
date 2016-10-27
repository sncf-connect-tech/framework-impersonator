package com.vsct.impersonator.http.integration;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class IntegrationTestUtils {

	private static HttpClient httpClient;

	static {
		httpClient = new HttpClient();
		httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
		try {
			httpClient.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String get(final String url) throws Exception {
		ContentExchange contentExchange = new ContentExchange();
		contentExchange.setURL(url);
		contentExchange.setMethod("GET");
		httpClient.send(contentExchange);
		contentExchange.waitForDone();
		return contentExchange.getResponseContent();
	}

	public static String post(final String url, final String contents) throws Exception {
		ContentExchange contentExchange = new ContentExchange();
		contentExchange.setURL(url);
		contentExchange.setMethod("POST");
		contentExchange.setRequestContent(new ByteArrayBuffer(contents.getBytes()));
		httpClient.send(contentExchange);
		contentExchange.waitForDone();
		return contentExchange.getResponseContent();
	}

	public static Server startServer(final int port, final String contentToServe) throws Exception {
		return startServer(port, contentToServe, null);
	}

	public static Server startServer(final int port, final String contentToServe, final String bodyToVerify) throws Exception {
		Server server = new Server(port);
		Handler handler = new AbstractHandler() {
			@Override
			public void handle(final String target, final Request baseRequest, final HttpServletRequest request,
					final HttpServletResponse response) throws IOException, ServletException {
				if (bodyToVerify != null) {
					assertEquals(bodyToVerify, readBodyFromRequest(request));
				}
				response.getWriter().write(contentToServe);
				((Request) request).setHandled(true);
			}
		};
		server.setHandler(handler);
		server.start();
		return server;
	}

	private static String readBodyFromRequest(final HttpServletRequest request) throws IOException {
		StringBuilder body = new StringBuilder();
		ServletInputStream inputStream = request.getInputStream();
		BufferedReader bodyReader = new BufferedReader(new InputStreamReader(inputStream));
		String line;
		while ((line = bodyReader.readLine()) != null) {
			body.append(line).append("\r\n");
		}
		return body.toString();
	}

	public static String buildContent(final int size) {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < size; i++) {
			content.append("x");
		}
		return content.toString();
	}

}
