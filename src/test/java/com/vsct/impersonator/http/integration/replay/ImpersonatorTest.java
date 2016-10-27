package com.vsct.impersonator.http.integration.replay;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

import com.vsct.impersonator.http.integration.IntegrationTestUtils;
import com.vsct.impersonator.http.integration.test.DefaultBounds;
import com.vsct.impersonator.http.integration.test.FixedUidRequestIdentifier;
import com.vsct.impersonator.http.message.RequestIdentifier;
import com.vsct.impersonator.http.message.storage.HttpExchangeCacheLoader;
import com.vsct.impersonator.http.message.storage.HttpExchangeSerialiser;
import com.vsct.impersonator.http.message.storage.HttpExchangeStorage;
import com.vsct.impersonator.http.message.storage.IHttpExchangeLoader;
import com.vsct.impersonator.http.message.storage.IHttpExchangeStore;
import com.vsct.impersonator.http.message.transform.DefaultRequestTransformer;
import com.vsct.impersonator.http.message.transform.DefaultResponseTransformer;
import com.vsct.impersonator.http.record.Recorder;
import com.vsct.impersonator.http.replay.Impersonator;

public class ImpersonatorTest {

	private static final int SERVER_PORT = 9900;
	private static final int RECORDER_PORT = SERVER_PORT + 1;
	private static final int IMPERSONATOR_PORT = SERVER_PORT + 2;

	@Test
	public void shouldReplayARecordedHttpExchange() throws Exception {
		File storageDir = new File("target/tmp");
		RequestIdentifier requestIdentifier = new FixedUidRequestIdentifier("w00t");
		HttpExchangeSerialiser serialiser = new HttpExchangeSerialiser(requestIdentifier, new DefaultRequestTransformer(),
				new DefaultResponseTransformer());
		IHttpExchangeStore exchangeStore = new HttpExchangeStorage(storageDir, serialiser);

		Server server = IntegrationTestUtils.startServer(SERVER_PORT, "w00t");

		Recorder recorder = Recorder.create("TEST", RECORDER_PORT, "127.0.0.1", SERVER_PORT, exchangeStore, false);
		String proxiedResponse = IntegrationTestUtils.post("http://127.0.0.1:" + RECORDER_PORT, "hot=bubbles");
		recorder.stop();

		server.stop();

		HttpExchangeSerialiser exchangeSerialiser = new HttpExchangeSerialiser(requestIdentifier);
		IHttpExchangeLoader exchangeLoader = new HttpExchangeCacheLoader(storageDir, exchangeSerialiser);
		Impersonator impersonator = Impersonator.create(IMPERSONATOR_PORT, exchangeLoader, new DefaultBounds());
		String impersonatedResponse = IntegrationTestUtils.post("http://127.0.0.1:" + IMPERSONATOR_PORT, "hot=bubbles");
		impersonator.stop();

		assertEquals(proxiedResponse, impersonatedResponse);

	}

	@Test
	public void shouldHandleLargeHtmlFilesCorrectly() throws Exception {
		File storageDir = storageDirFor("/large-html.request");
		RequestIdentifier requestIdentifier = new FixedUidRequestIdentifier("large-html");
		HttpExchangeSerialiser exchangeSerialiser = new HttpExchangeSerialiser(requestIdentifier);
		IHttpExchangeLoader exchangeLoader = new HttpExchangeCacheLoader(storageDir, exchangeSerialiser);

		Impersonator impersonator = Impersonator.create(IMPERSONATOR_PORT, exchangeLoader, new DefaultBounds());
		String returned = sendRequestFromFile(new File(storageDir, "large-html.request"));
		impersonator.stop();

		assertEquals(withoutHeaders(returned), withoutHeaders(getResponseBody(storageDir)));
	}

	private String sendRequestFromFile(final File file) throws IOException, UnsupportedEncodingException {
		final Socket socket = new Socket();
		socket.connect(new InetSocketAddress("127.0.0.1", IMPERSONATOR_PORT));
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
		wr.write(FileUtils.readFileToString(file));
		wr.flush();
		String returned = IOUtils.toString(socket.getInputStream(), "utf-8");
		socket.close();
		return returned;
	}

	private File storageDirFor(final String file) {
		URL resource = getClass().getResource(file);
		File path = new File(resource.getFile());
		return path.getParentFile();
	}

	private String withoutHeaders(final String body) {
		return body.substring(body.indexOf("<!DOCTYPE"));
	}

	private String getResponseBody(final File storageDir) throws IOException {
		return FileUtils.readFileToString(new File(storageDir, "large-html.response"), "utf-8");
	}

}
