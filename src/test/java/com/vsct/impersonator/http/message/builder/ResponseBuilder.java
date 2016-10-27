package com.vsct.impersonator.http.message.builder;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class ResponseBuilder {
	private int responseCode;
	private Map<String, String> headers;
	private String body;
	private String reasonPhrase;
	private static final String LINE_SEPARATOR = "\r\n";
	private String protocol;

	public ResponseBuilder() {
		protocol = "HTTP/1.1";
		body = "hot bubbles";
		headers = new HashMap<>();
		headers.put("Server", "w00tServer");
		headers.put("Content-Length", "100000");
	}

	public ResponseBuilder withResponseCode(int responseCode) {
		this.responseCode = responseCode;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder request = new StringBuilder().append(protocol).append(" ").append(responseCode).append(" ").append(reasonPhrase)
				.append(LINE_SEPARATOR);
		for (String headerName : headers.keySet()) {
			request.append(headerName).append(": ").append(headers.get(headerName)).append(LINE_SEPARATOR);
		}
		request.append(LINE_SEPARATOR);
		request.append(body);
		return request.toString();
	}

	public Reader toReader() {
		return new StringReader(toString());
	}

	public ResponseBuilder withHeader(String name, String value) {
		headers.put(name, value);
		return this;
	}

	public ResponseBuilder withBody(String body) {
		this.body = body;
		return this;
	}

	public ResponseBuilder withReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
		return this;
	}

	public ResponseBuilder withoutHeaders() {
		headers.clear();
		return this;
	}
}
