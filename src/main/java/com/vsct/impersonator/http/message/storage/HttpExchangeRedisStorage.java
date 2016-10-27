package com.vsct.impersonator.http.message.storage;

import org.apache.log4j.Logger;

import com.vsct.impersonator.http.message.HttpExchange;

public class HttpExchangeRedisStorage extends AbstractHttpRedisStorage implements IHttpExchangeStore {

	private static final Logger LOGGER = Logger.getLogger(HttpExchangeStorage.class);

	public HttpExchangeRedisStorage(final RedisConfiguration config, final HttpExchangeSerialiser serialiser) {
		super(config, serialiser);
	}

	/**
	 * @see com.vsct.impersonator.http.message.storage.IHttpExchangeStore#store(com.vsct.impersonator.http.message.HttpExchange)
	 */
	@Override
	public void store(final HttpExchange exchange) {
		if (exchange != null) {
			String key = serialiser.identify(exchange.request());
			save(key + ".request", serialiser.serialise(exchange.request()));
			save(key + ".response", serialiser.serialise(exchange.response()));
		} else {
			LOGGER.warn("Attempting to save a null http exchange");
		}
	}

}
