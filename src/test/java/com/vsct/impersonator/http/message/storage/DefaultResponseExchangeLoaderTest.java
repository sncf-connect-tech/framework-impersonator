package com.vsct.impersonator.http.message.storage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.vsct.impersonator.http.message.transform.DefaultRequestTransformer;
import com.vsct.impersonator.http.message.transform.DefaultResponseTransformer;

@RunWith(MockitoJUnitRunner.class)
public class DefaultResponseExchangeLoaderTest {
	private static final String FILE_TEST = "/DefaultResponseExchangeLoaderTest.response";

	private DefaultResponseExchangeLoader testLoader;

	@Mock
	private IHttpExchangeLoader defaultExchangeLoaderMock;

	@Before
	public void init() {
		HttpExchangeSerialiser serialiser = new HttpExchangeSerialiser(null, new DefaultRequestTransformer(),
				new DefaultResponseTransformer());

		testLoader = new DefaultResponseExchangeLoader(defaultExchangeLoaderMock, serialiser, FILE_TEST);
	}

	@Test
	public void testLoadResponseFor_response_is_found() throws Exception {
		HttpRequest request = mock(HttpRequest.class);
		HttpResponse response = mock(HttpResponse.class);

		when(response.getStatus()).thenReturn(HttpResponseStatus.OK);
		when(defaultExchangeLoaderMock.loadResponseFor(request)).thenReturn(response);

		HttpResponse finalResponse = testLoader.loadResponseFor(request);

		assertEquals(response, finalResponse);
	}

	@Test
	public void testLoadResponseFor_response_not_found_default_found() throws Exception {
		HttpRequest request = mock(HttpRequest.class);
		HttpResponse response = mock(HttpResponse.class);

		when(response.getStatus()).thenReturn(HttpResponseStatus.NOT_FOUND);
		when(defaultExchangeLoaderMock.loadResponseFor(request)).thenReturn(response);

		HttpResponse finalResponse = testLoader.loadResponseFor(request);

		assertEquals(HttpResponseStatus.OK, finalResponse.getStatus());
	}
}
