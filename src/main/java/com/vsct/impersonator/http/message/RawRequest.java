package com.vsct.impersonator.http.message;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class RawRequest extends Message {
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	private String method;
	private String protocol;
	private String requestUri;
	private final Map<String, String> parameters;

	public RawRequest(final HttpRequest request) {
		parameters = new LinkedHashMap<>();
		this.method = request.getMethod().getName();
		this.protocol = request.getProtocolVersion().getText();
		this.requestUri = request.getUri();
		loadUriData(request.getUri());
		loadHeaders(request);
		try {
			loadBody(request);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void loadHeaders(final HttpRequest request) {
		for (String header : request.getHeaderNames()) {
			setHeader(header, request.getHeader(header));
		}
	}

	private void loadBody(final HttpRequest request) throws IOException {
		setBody(request.getContent().toString(UTF_8));
	}

	public RawRequest(final String contents) {
		this(new StringReader(contents));
	}

	public RawRequest(final Reader reader) {
		parameters = new LinkedHashMap<>();
		load(reader);
	}

	@Override
	protected void setStartLineElements(final String firstElement, final String secondElement, final String thirdElement) {
		method = firstElement;
		protocol = thirdElement;
		loadUriData(secondElement);
	}

	private void loadUriData(final String fullUri) {
		String[] uriParts = fullUri.split("\\?");
		loadUri(uriParts[0]);
		if (uriParts.length > 1) {
			loadParameters(uriParts[1]);
		}
	}

	@Override
	protected String getStartLine() {
		return String.format("%s %s %s", method, getFullUri(), protocol);
	}

	public String getFullUri() {
		if (!getMethod().equalsIgnoreCase("GET")) {
			return requestUri;
		}
		StringBuilder resultUri = new StringBuilder(requestUri);
		if (parameters.size() > 0) {
			resultUri.append("?");
			int current = 0;
			for (String parameter : parameters.keySet()) {
				resultUri.append(parameter).append("=").append(parameters.get(parameter));
				if (current < parameters.size() - 1) {
					resultUri.append("&");
				}
				current++;
			}
		}
		return resultUri.toString();
	}

	private void loadUri(final String fullUri) {
		int paramsPosition = fullUri.indexOf('?');
		requestUri = (paramsPosition > 0) ? fullUri.substring(0, paramsPosition) : fullUri;
	}

	private void loadParameters(final String parameterString) {
		if (parameterString != null) {
			for (String parameterPair : parameterString.split("&")) {
				String[] parts = parameterPair.split("=");
				if (parts.length == 2) {
					parameters.put(parts[0], parts[1]);
				}
			}
		}
	}

	public String getMethod() {
		return method;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getParameter(final String name) {
		return parameters.get(name);
	}

	public void setParameter(final String parameterName, final String parameterValue) {
		parameters.put(parameterName, parameterValue);
	}

	public Set<String> getParameterNames() {
		return parameters.keySet();
	}

	public void removeParameter(final String parameterName) {
		parameters.remove(parameterName);
	}

	public String getProtocolVersion() {
		return protocol.split("/")[1];
	}

	public String getRequestUri() {
		return requestUri;
	}

	public void setRequestUri(final String requestUri) {
		this.requestUri = requestUri;
	}

	public HttpRequest toHttpRequest() {
		HttpRequest request = new DefaultHttpRequest(HttpVersion.valueOf(protocol), HttpMethod.valueOf(method), requestUri);
		for (String headerName : getHeaderNames()) {
			request.setHeader(headerName, getHeader(headerName));
		}
		ChannelBuffer content = ChannelBuffers.copiedBuffer(getBody(), UTF_8);
		request.setContent(content);
		return request;
	}

}
