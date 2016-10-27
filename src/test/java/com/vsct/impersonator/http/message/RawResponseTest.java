package com.vsct.impersonator.http.message;

import com.vsct.impersonator.http.message.builder.ResponseBuilder;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class RawResponseTest {

	private static final String LINE_TERMINATOR = "\r\n";

	@Test
	public void shouldIdentifyResponseCode() {
		ResponseBuilder builder = new ResponseBuilder().withResponseCode(200);

		RawResponse response = new RawResponse(builder.toReader());

		assertThat(response.getStatusCode(), is(200));
	}

	@Test
	public void shouldParseResponseReasonPhrase() {
		ResponseBuilder builder = new ResponseBuilder().withReasonPhrase("OK");

		RawResponse response = new RawResponse(builder.toReader());

		assertThat(response.getReasonPhrase(), is("OK"));
	}

	@Test
	public void shouldParseResponseHeaders() {
		ResponseBuilder builder = new ResponseBuilder().withHeader("Connection", "Keep-Alive");

		RawResponse response = new RawResponse(builder.toReader());

		assertThat(response.getHeader("Connection"), is("Keep-Alive"));
	}

	@Test
	public void shouldParseResponseBody() {
		ResponseBuilder builder = new ResponseBuilder().withBody("w00t" + LINE_TERMINATOR);

		RawResponse response = new RawResponse(builder.toReader());

		assertThat(response.getBody(), is("w00t" + LINE_TERMINATOR));
	}

	@Test
	public void shouldChangeContentLengthWhenBodyChanges() {
		RawResponse response = new RawResponse(new ResponseBuilder().withBody("w00t").toReader());
		assertThat(response.getHeader("Content-Length"), is("4"));

		response.setBody("to a longer string");

		assertThat(response.getHeader("Content-Length"), is("18"));
	}

	@Test
	public void shouldReturnAProperlyFormattedResponse() {
		ResponseBuilder builder = new ResponseBuilder().withoutHeaders();
		builder.withResponseCode(200).withReasonPhrase("OK").withBody("w00t" + LINE_TERMINATOR);

		RawResponse response = new RawResponse(builder.toReader());

		assertThat(response.toString(), startsWith("HTTP/1.1 200 OK" + LINE_TERMINATOR));
		assertThat(response.toString(), containsString("Content-Length: 6" + LINE_TERMINATOR));
		assertThat(response.toString(), endsWith(LINE_TERMINATOR + LINE_TERMINATOR + "w00t" + LINE_TERMINATOR));
	}

	@Test
	public void shouldBuildResponseFromHttpServletResponse() throws Exception {
		HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		httpResponse.addHeader("hot", "bubbles");
		httpResponse.setContent(ChannelBuffers.copiedBuffer("w00t" + LINE_TERMINATOR, RawResponse.UTF_8));

		RawResponse response = new RawResponse(httpResponse);

		assertThat(response.toString(), startsWith("HTTP/1.1 200 OK" + LINE_TERMINATOR));
		assertThat(response.toString(), containsString("Content-Length: 6" + LINE_TERMINATOR));
		assertThat(response.toString(), containsString("hot: bubbles" + LINE_TERMINATOR));
		assertThat(response.toString(), endsWith(LINE_TERMINATOR + LINE_TERMINATOR + "w00t" + LINE_TERMINATOR));
	}
}
