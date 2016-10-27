package com.vsct.impersonator.http.message.builder;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RequestBuilder {
	private String uri;
	private String method;
	private String protocol;
	private String body;
	private Map<String, String> headers;
	private Map<String, String> parameters;
	private static final String LINE_SEPARATOR = "\r\n";

	public RequestBuilder() {
		method = "GET";
		uri = "/";
		protocol = "HTTP/1.1";
		body = "hot bubbles";
		headers = new HashMap<>();
		headers.put("Host", "127.0.0.1");
		headers.put("User-Agent", "w00t browser");
		parameters = new LinkedHashMap<>();
	}

	public RequestBuilder withParameter(String name, String value) {
		parameters.put(name, value);
		return this;
	}

	public Reader toReader() {
		return new StringReader(toString());
	}

	@Override
	public String toString() {
		StringBuilder request = new StringBuilder().append(method).append(" ").append(getUriWithParameters()).append(" ").append(protocol)
				.append(LINE_SEPARATOR);
		for (String headerName : headers.keySet()) {
			request.append(headerName).append(": ").append(headers.get(headerName)).append(LINE_SEPARATOR);
		}
		request.append(LINE_SEPARATOR);
		if (body != null) {
			request.append(body);
		}
		return request.toString();
	}

	private String getUriWithParameters() {
		StringBuilder resultUri = new StringBuilder(uri);
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

	public RequestBuilder withHeader(String name, String value) {
		headers.put(name, value);
		return this;
	}

	public RequestBuilder withMethod(String method) {
		this.method = method;
		return this;
	}

	public RequestBuilder withRequestUri(String uri) {
		this.uri = uri;
		return this;
	}

	public RequestBuilder withProtocol(String protocol) {
		this.protocol = protocol;
		return this;
	}

	public RequestBuilder withBody(String body) {
		this.body = body;
		return this;
	}

	public RequestBuilder withoutParameters() {
		parameters.clear();
		return this;
	}

	public RequestBuilder withoutHeaders() {
		headers.clear();
		return this;
	}

	public RequestBuilder withoutBody() {
		this.body = null;
		return this;
	}
}
