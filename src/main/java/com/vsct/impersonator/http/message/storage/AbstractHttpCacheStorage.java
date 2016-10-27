package com.vsct.impersonator.http.message.storage;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public abstract class AbstractHttpCacheStorage {

	private static final String ENCODING = "UTF-8";

	protected Logger log = Logger.getLogger(getClass());
	protected File storageDir;
	protected HttpExchangeSerialiser serialiser;

	public AbstractHttpCacheStorage(final File storageDir, final HttpExchangeSerialiser serialiser) {
		ensureDirectoryExists(storageDir);
		this.storageDir = storageDir;
		this.serialiser = serialiser;
	}

	protected void ensureDirectoryExists(final File directory) {
		if (!directory.exists() && !directory.mkdirs()) {
			log.error(String.format("failed to create storage directory at '%s'", directory.getAbsolutePath()));
		}
	}

	protected void save(final String contents, final File unknownFile) {
		try {
			FileUtils.writeStringToFile(unknownFile, contents, ENCODING);
			log.debug("Store on disk : " + unknownFile);
		} catch (IOException e) {
			log.error("error while saving file : " + unknownFile, e);
			throw new RuntimeException(e);
		}
	}

	protected HttpResponse notFound() {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	}

	protected String contents(final File recorded) {
		try {
			return FileUtils.readFileToString(recorded, ENCODING);
		} catch (IOException e) {
			log.error("error while loading file : " + recorded, e);
			throw new RuntimeException(e);
		}
	}

}