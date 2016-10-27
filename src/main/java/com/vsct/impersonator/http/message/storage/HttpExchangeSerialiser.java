package com.vsct.impersonator.http.message.storage;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.vsct.impersonator.http.message.RawRequest;
import com.vsct.impersonator.http.message.RawResponse;
import com.vsct.impersonator.http.message.RequestIdentifier;
import com.vsct.impersonator.http.message.transform.DefaultRequestTransformer;
import com.vsct.impersonator.http.message.transform.DefaultResponseTransformer;
import com.vsct.impersonator.http.message.transform.RequestTransformer;
import com.vsct.impersonator.http.message.transform.ResponseTransformer;

public class HttpExchangeSerialiser {
	private final RequestIdentifier requestIdentifier;
	private final RequestTransformer requestTransformer;
	private final ResponseTransformer responseTransformer;

	public HttpExchangeSerialiser(final RequestIdentifier requestIdentifier) {
		this(requestIdentifier, new DefaultRequestTransformer(), new DefaultResponseTransformer());
	}

	public HttpExchangeSerialiser(final RequestIdentifier requestIdentifier, final RequestTransformer requestTransformer,
			final ResponseTransformer responseTransformer) {
		this.requestIdentifier = requestIdentifier;
		this.requestTransformer = requestTransformer;
		this.responseTransformer = responseTransformer;
	}

	public String identify(final HttpRequest request) {
		RawRequest rawRequest = new RawRequest(request);
		requestTransformer.transform(rawRequest);
		return requestIdentifier.calculateUid(rawRequest);
	}

	public HttpResponse deserialiseResponse(final String contents) {
		RawResponse rawResponse = new RawResponse(contents);
		return rawResponse.toHttpResponse();
	}

	public String serialise(final HttpRequest request) {
		RawRequest rawRequest = new RawRequest(request);
		requestTransformer.transform(rawRequest);
		return rawRequest.toString();
	}

	public HttpRequest deserialiseRequest(final String contents) {
		RawRequest rawRequest = new RawRequest(contents);
		return rawRequest.toHttpRequest();
	}

	public String serialise(final HttpResponse response) {
		RawResponse rawResponse = new RawResponse(response);
		responseTransformer.transform(rawResponse);
		return rawResponse.toString();
	}

}
