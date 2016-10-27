package com.vsct.impersonator.http.message;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Test;

import com.vsct.impersonator.http.message.builder.RequestBuilder;

public class RawRequestTest {

	private static final String LINE_SEPARATOR = "\r\n";

	@Test
	public void shouldParseRequestParameters() {
		RequestBuilder builder = new RequestBuilder().withParameter("query", "w00t").withParameter("resultsPerPage", "100");

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.getParameter("query"), is("w00t"));
		assertThat(request.getParameter("resultsPerPage"), is("100"));
	}

	@Test
	public void shouldParseRequestHeaders() {
		RequestBuilder builder = new RequestBuilder().withHeader("Host", "127.0.0.1:9090")
				.withHeader("Content-Type", "application/x-www-form-urlencoded").withHeader("User-Agent", "Jakarta Commons-HttpClient/3.1");

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.getHeader("Host"), is("127.0.0.1:9090"));
		assertThat(request.getHeader("Content-Type"), is("application/x-www-form-urlencoded"));
		assertThat(request.getHeader("User-Agent"), is("Jakarta Commons-HttpClient/3.1"));
	}

	@Test
	public void shouldParseRequestMethod() {
		RequestBuilder builder = new RequestBuilder().withMethod("POST");

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.getMethod(), is("POST"));
	}

	@Test
	public void shouldParseRequestUri() {
		RequestBuilder builder = new RequestBuilder().withRequestUri("/");

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.getRequestUri(), is("/"));
	}

	@Test
	public void shouldParseRequestProtocol() {
		RequestBuilder builder = new RequestBuilder().withProtocol("HTTP/1.1");

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.getProtocol(), is("HTTP/1.1"));
	}

	@Test
	public void shouldParseBody() {
		RequestBuilder builder = new RequestBuilder().withBody("abc=123" + LINE_SEPARATOR);

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.getBody(), is("abc=123" + LINE_SEPARATOR));
	}

	@Test
	public void shouldHaveConfigurableParameters() {
		Reader reader = new RequestBuilder().withParameter("Host", "localhost").toReader();
		RawRequest request = new RawRequest(reader);

		request.setParameter("Host", "facepalm.org");

		assertThat(request.getParameter("Host"), is("facepalm.org"));
	}

	@Test
	public void shouldHaveConfigurableHeaders() {
		Reader reader = new RequestBuilder().withHeader("Host", "localhost").toReader();
		RawRequest request = new RawRequest(reader);

		request.setHeader("Host", "facepalm.org");

		assertThat(request.getHeader("Host"), is("facepalm.org"));
	}

	@Test
	public void shouldProvideASetViewOfAllParameters() {
		RequestBuilder builder = new RequestBuilder().withoutParameters();
		builder.withParameter("maxResults", "10");
		RawRequest request = new RawRequest(builder.toReader());

		Set<String> parameters = request.getParameterNames();

		assertThat(parameters.contains("maxResults"), is(true));
	}

	@Test
	public void shouldProvideASetViewOfAllHeaders() {
		RequestBuilder builder = new RequestBuilder().withoutHeaders();
		builder.withHeader("Host", "localhost");
		RawRequest request = new RawRequest(builder.toReader());

		Set<String> headers = request.getHeaderNames();

		assertThat(headers.contains("Host"), is(true));
	}

	@Test
	public void shouldAllowParameterDeletion() {
		Reader reader = new RequestBuilder().withParameter("query", "abc").toReader();
		RawRequest request = new RawRequest(reader);

		request.removeParameter("query");

		assertNull(request.getParameter("Host"));
	}

	@Test
	public void shouldAllowHeaderDeletion() {
		Reader reader = new RequestBuilder().withHeader("Host", "localhost").toReader();
		RawRequest request = new RawRequest(reader);

		request.removeHeader("Host");

		assertNull(request.getHeader("Host"));
	}

	@Test
	public void shouldIdentifyTheProtocolVersion() {
		RequestBuilder builder = new RequestBuilder().withProtocol("HTTP/1.1");

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.getProtocolVersion(), is("1.1"));
	}

	@Test
	public void shouldReturnAProperlyFormattedRequest() {
		String expectedRequest = new StringBuilder().append("GET /test?a=b&c=d HTTP/1.1").append(LINE_SEPARATOR).append("Host: localhost")
				.append(LINE_SEPARATOR).append(LINE_SEPARATOR).append("w00t").append(LINE_SEPARATOR).toString();

		RequestBuilder builder = new RequestBuilder().withoutHeaders();
		builder.withMethod("GET").withRequestUri("/test").withParameter("a", "b").withParameter("c", "d").withHeader("Host", "localhost")
				.withBody("w00t" + LINE_SEPARATOR);

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.toString(), is(expectedRequest));
	}

	@Test
	public void shouldReturnAProperlyFormattedRequestWithoutBody() {
		String expectedRequest = new StringBuilder().append("GET /test?a=b&c=d HTTP/1.1").append(LINE_SEPARATOR).append("Host: localhost")
				.append(LINE_SEPARATOR).append(LINE_SEPARATOR).toString();

		RequestBuilder builder = new RequestBuilder().withoutHeaders();
		builder.withMethod("GET").withRequestUri("/test").withParameter("a", "b").withParameter("c", "d").withHeader("Host", "localhost")
				.withoutBody();

		RawRequest request = new RawRequest(builder.toReader());

		assertThat(request.toString(), is(expectedRequest));
	}

	@Test
	public void shouldTranslateParametersFromHttpServletRequest() {
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html?hot=bubbles");

		RawRequest request = new RawRequest(httpRequest);

		assertThat(request.getParameter("hot"), is("bubbles"));
	}

	@Test
	public void shouldTranslateMethodFromHttpServletRequest() {
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html");

		RawRequest request = new RawRequest(httpRequest);

		assertThat(request.getMethod(), is("GET"));
	}

	@Test
	public void shouldTranslateRequestUriFromHttpServletRequest() {
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html");

		RawRequest request = new RawRequest(httpRequest);

		assertThat(request.getRequestUri(), is("/index.html"));
	}

	@Test
	public void shouldTranslateBodyFromHttpServletRequest() throws IOException {
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html");
		httpRequest.setContent(ChannelBuffers.copiedBuffer("w00t" + LINE_SEPARATOR + LINE_SEPARATOR, RawRequest.UTF_8));

		RawRequest request = new RawRequest(httpRequest);

		assertThat(request.getBody(), is("w00t" + LINE_SEPARATOR + LINE_SEPARATOR));
	}

	@Test
	public void shouldTranslateHeadersFromHttpServletRequest() {
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html");
		httpRequest.addHeader("User-Agent", "Jakarta Commons-HttpClient/3.1");

		RawRequest request = new RawRequest(httpRequest);

		assertThat(request.getHeader("User-Agent"), is("Jakarta Commons-HttpClient/3.1"));
	}

	@Test
	public void shouldBuildFullGetUriWithParameters() {
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/index.html?action=rm-rf");

		RawRequest request = new RawRequest(httpRequest);

		assertThat(request.getFullUri(), is("/index.html?action=rm-rf"));
	}

	@Test
	public void test_parse_simple_url() throws Exception {
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://www.test.com/plip/plop");
		RawRequest rawRequest = new RawRequest(httpRequest);

		assertEquals("GET", rawRequest.getMethod());
		assertEquals("1.1", rawRequest.getProtocolVersion());
		assertEquals("http://www.test.com/plip/plop", rawRequest.getRequestUri());
		assertEquals("http://www.test.com/plip/plop", rawRequest.getFullUri());
		assertTrue(rawRequest.getParameterNames().isEmpty());
	}

	@Test
	public void test_parse_url_and_request() throws Exception {
		HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
				"http://www.test.com?param1=value1&param2=value2,value3");
		RawRequest rawRequest = new RawRequest(httpRequest);

		assertEquals("http://www.test.com", rawRequest.getRequestUri());
		assertEquals("http://www.test.com?param1=value1&param2=value2,value3", rawRequest.getFullUri());
		assertEquals(2, rawRequest.getParameterNames().size());
		assertEquals("value1", rawRequest.getParameter("param1"));
		assertEquals("value2,value3", rawRequest.getParameter("param2"));
	}
}
