package com.vsct.impersonator.http.message;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class RawResponse extends Message {
	public static final Charset UTF_8 = Charset.forName("UTF-8");

	private Integer statusCode;
	private String reasonPhrase;
	private String protocol;

	public RawResponse(final HttpResponse response) {
		StringBuilder responseText = new StringBuilder();
		HttpResponseStatus responseStatus = response.getStatus();
		responseText.append(String.format("%s %d %s", "HTTP/1.1", responseStatus.getCode(), responseStatus.getReasonPhrase())).append(
				"\r\n");
		for (String headerName : response.getHeaderNames()) {
			String headerValue = response.getHeader(headerName);
			responseText.append(headerName).append(": ").append(headerValue).append("\r\n");
		}
		// TODO: should we use the correct content-type?
		responseText.append("\r\n").append(response.getContent().toString(UTF_8));
		load(new StringReader(responseText.toString()));
	}

	public RawResponse(final String contents) {
		this(new StringReader(contents));
	}

	public RawResponse(final Reader reader) {
		load(reader);
	}

	@Override
	protected void setStartLineElements(final String firstElement, final String secondElement, final String thirdElement) {
		protocol = firstElement;
		statusCode = Integer.valueOf(secondElement);
		reasonPhrase = thirdElement;
	}

	@Override
	public void setBody(final String body) {
		super.setBody(body);
		setHeader("Content-Length", Integer.toString(body.getBytes(UTF_8).length));
	}

	@Override
	protected String getStartLine() {
		return String.format("%s %d %s", protocol, statusCode, reasonPhrase);
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public String getProtocol() {
		return protocol;
	}

	private Charset getEncoding() {
		String contentType = getHeader("Content-Type");
		String charset = "charset=";
		if (contentType != null && contentType.contains(charset)) {
			return Charset.forName(contentType.substring(contentType.indexOf(charset) + charset.length()));
		}
		return UTF_8;
	}

	public HttpResponse toHttpResponse() {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		for (String headerName : getHeaderNames()) {
			response.setHeader(headerName, getHeader(headerName));
		}
		ChannelBuffer content = ChannelBuffers.copiedBuffer(getBody(), getEncoding());
		response.setContent(content);
		return response;
	}

}
