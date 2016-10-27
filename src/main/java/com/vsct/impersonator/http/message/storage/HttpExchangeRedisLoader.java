package com.vsct.impersonator.http.message.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.vsct.impersonator.http.message.HttpExchange;
import com.vsct.impersonator.http.util.Loggers;

public class HttpExchangeRedisLoader extends AbstractHttpRedisStorage implements IHttpExchangeLoader {

	public HttpExchangeRedisLoader(final RedisConfiguration config, final HttpExchangeSerialiser serialiser) {
		super(config, serialiser);
	}

	/**
	 * @see com.vsct.impersonator.http.message.storage.IHttpExchangeLoader#loadResponseFor(org.jboss.netty.handler.codec.http.HttpRequest)
	 */
	@Override
	public HttpResponse loadResponseFor(final HttpRequest request) {
		String uid = serialiser.identify(request);
		String serializedResponse = retrieve(uid + ".response");

		if (serializedResponse != null) {
			log.debug("recorded message found with id: " + uid);
			return serialiser.deserialiseResponse(serializedResponse);
		}

		Loggers.UNKNOWN_QUERY_LOGGER.error("unknown request : " + request.getUri());
		return notFound();
	}

	@Override
	public List<HttpExchange> all() {
		Set<String> keys = getAllKeys();
		List<HttpExchange> results = new ArrayList<>();
		for (String key : keys) {
			HttpRequest request = serialiser.deserialiseRequest(retrieve(key));
			HttpResponse response = serialiser.deserialiseResponse(retrieve(key.replaceAll("request$", "response")));
			results.add(new HttpExchange(request, response));
		}
		return results;
	}

}
