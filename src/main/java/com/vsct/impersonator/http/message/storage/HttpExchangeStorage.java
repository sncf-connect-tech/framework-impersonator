package com.vsct.impersonator.http.message.storage;

import java.io.File;

import org.apache.log4j.Logger;

import com.vsct.impersonator.http.message.HttpExchange;

public class HttpExchangeStorage extends AbstractHttpCacheStorage implements IHttpExchangeStore {

	private static final Logger LOGGER = Logger.getLogger(HttpExchangeStorage.class);

	public HttpExchangeStorage(File storageDir, HttpExchangeSerialiser serialiser) {
		super(storageDir, serialiser);
	}

	/**
	 * @see com.vsct.impersonator.http.message.storage.IHttpExchangeStore#store(com.vsct.impersonator.http.message.HttpExchange)
	 */
	@Override
	public void store(HttpExchange exchange) {
		if (exchange != null) {
			String filename = storageDir.getAbsolutePath() + "/" + serialiser.identify(exchange.request());
			save(serialiser.serialise(exchange.request()), new File(filename + ".request"));
			save(serialiser.serialise(exchange.response()), new File(filename + ".response"));
		} else {
			LOGGER.warn("Attempting to save a null http exchange");
		}
	}

}
