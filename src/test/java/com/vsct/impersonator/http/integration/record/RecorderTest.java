package com.vsct.impersonator.http.integration.record;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.junit.Test;

import com.vsct.impersonator.http.integration.IntegrationTestUtils;
import com.vsct.impersonator.http.integration.test.FixedUidRequestIdentifier;
import com.vsct.impersonator.http.message.RequestIdentifier;
import com.vsct.impersonator.http.message.storage.HttpExchangeSerialiser;
import com.vsct.impersonator.http.message.storage.HttpExchangeStorage;
import com.vsct.impersonator.http.message.storage.IHttpExchangeStore;
import com.vsct.impersonator.http.message.transform.DefaultRequestTransformer;
import com.vsct.impersonator.http.message.transform.DefaultResponseTransformer;
import com.vsct.impersonator.http.record.Recorder;

public class RecorderTest {

	private static final int SERVER_PORT = 9920;
	private static final int RECORDER_PORT = SERVER_PORT + 1;

	@Test
	public void shouldRecord() throws Exception {
		RequestIdentifier requestIdentifier = new FixedUidRequestIdentifier("w00t");
		HttpExchangeSerialiser serialiser = new HttpExchangeSerialiser(requestIdentifier, new DefaultRequestTransformer(),
				new DefaultResponseTransformer());
		IHttpExchangeStore exchangeStore = new HttpExchangeStorage(new File("target/tmp"), serialiser);

		Server server = IntegrationTestUtils.startServer(SERVER_PORT, "w00t", IntegrationTestUtils.buildContent(65535) + "\r\n");
		Recorder recorder = Recorder.create("TEST", RECORDER_PORT, "127.0.0.1", SERVER_PORT, exchangeStore, false);
		String returned = IntegrationTestUtils.post("http://127.0.0.1:" + RECORDER_PORT, IntegrationTestUtils.buildContent(65535));
		recorder.stop();
		server.stop();

		assertEquals("w00t", returned);
	}

}
