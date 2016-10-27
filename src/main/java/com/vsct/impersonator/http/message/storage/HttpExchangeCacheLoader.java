package com.vsct.impersonator.http.message.storage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.vsct.impersonator.http.message.HttpExchange;
import com.vsct.impersonator.http.util.Loggers;

public class HttpExchangeCacheLoader extends AbstractHttpCacheStorage implements IHttpExchangeLoader {

	private ConcurrentHashMap<String, HttpResponse> httpExchangeCache = new ConcurrentHashMap<>();

	private static final FilenameFilter REQUEST_FILES = new FilenameFilter() {
		@Override
		public boolean accept(final File dir, final String name) {
			return name.toLowerCase().endsWith(".request");
		}
	};

	public HttpExchangeCacheLoader(final File storageDir, final HttpExchangeSerialiser serialiser) {
		super(storageDir, serialiser);
		this.httpExchangeCache = preload();
	}

	/**
	 * @see com.vsct.impersonator.http.message.storage.IHttpExchangeLoader#loadResponseFor(org.jboss.netty.handler.codec.http.HttpRequest)
	 */
	@Override
	public HttpResponse loadResponseFor(final HttpRequest request) {
		String uid = serialiser.identify(request);
		HttpResponse response = httpExchangeCache.get(uid);
		if (response != null) {
			log.debug("recorded message found with id: " + uid);
			return response;
		}

		Loggers.UNKNOWN_QUERY_LOGGER.error("unknown request : " + request.getUri());
		return notFound();
	}

	private ConcurrentHashMap<String, HttpResponse> preload() {
		log.debug("initial preload :");
		ConcurrentHashMap<String, HttpResponse> results = new ConcurrentHashMap<>();
		for (File requestFile : storageDir.listFiles(REQUEST_FILES)) {
			File responseFile = new File(requestFile.getAbsolutePath().replaceAll("request$", "response"));
			String requestName = requestFile.getName();
			String uid = requestName.substring(0, requestName.lastIndexOf(".request"));
			log.debug("-> loading: " + responseFile.getAbsolutePath());
			results.put(uid, serialiser.deserialiseResponse(contents(responseFile)));
		}
		return results;
	}

	/**
	 * @see com.vsct.impersonator.http.message.storage.IHttpExchangeLoader#all()
	 */
	@Override
	public List<HttpExchange> all() {
		List<HttpExchange> results = new ArrayList<>();
		for (File requestFile : storageDir.listFiles(REQUEST_FILES)) {
			File responseFile = new File(requestFile.getAbsolutePath().replaceAll("request$", "response"));
			results.add(new HttpExchange(serialiser.deserialiseRequest(contents(requestFile)), serialiser
					.deserialiseResponse(contents(responseFile))));
		}
		return results;
	}

}
