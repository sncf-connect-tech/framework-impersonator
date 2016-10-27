package com.vsct.impersonator.http.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Message {
	protected static final String LINE_TERMINATOR = "\r\n";
	private String body;
	private Map<String, String> headers;

	public Message() {
		headers = new HashMap<>();
	}

	protected void load(Reader reader) {
		BufferedReader bufferedReader = new BufferedReader(reader);
		try {
			loadStartLine(bufferedReader);
			loadHeaders(bufferedReader);
			loadBody(bufferedReader);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract void setStartLineElements(String firstElement, String secondElement, String thirdElement);

	private void loadStartLine(BufferedReader reader) throws IOException {
		Matcher matcher = getPatternGroups(reader.readLine(), "^(.+?) (.+?) (.+?)$");
		setStartLineElements(matcher.group(1), matcher.group(2), matcher.group(3));
	}

	private Matcher getPatternGroups(String line, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			return matcher;
		}
		throw new RuntimeException("Invalid HTTP file. Line \"" + line + "\" does not seem valid");
	}

	private void loadHeaders(BufferedReader reader) throws IOException {
		String line;
		Matcher matcher;
		while (true) {
			line = reader.readLine();
			if (line == null || line.equals("")) {
				return;
			}
			matcher = getPatternGroups(line, "^(.+?): (.+)$");
			setHeader(matcher.group(1), matcher.group(2));
		}
	}

	protected void loadBody(BufferedReader reader) throws IOException {
		if (reader != null) {
			StringBuilder parsedBody = new StringBuilder("");
			char cbuf[] = new char[1024];
			int len = 0;
			while ((len = reader.read(cbuf)) != -1) {
				parsedBody.append(cbuf, 0, len);
			}
			setBody(parsedBody.toString());
		}
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getHeader(String headername) {
		return headers.get(headername);
	}

	public void setHeader(String name, String value) {
		headers.put(name, value);
	}

	public void removeHeader(String headername) {
		headers.remove(headername);
	}

	public Set<String> getHeaderNames() {
		return headers.keySet();
	}

	@Override
	public String toString() {
		StringBuilder request = new StringBuilder().append(getStartLine()).append(LINE_TERMINATOR);
		for (String headerName : headers.keySet()) {
			request.append(headerName).append(": ").append(headers.get(headerName)).append(LINE_TERMINATOR);
		}
		request.append(LINE_TERMINATOR);
		if (body != null && body.length() > 0) {
			request.append(body);
		}
		return request.toString();
	}

	protected abstract String getStartLine();

	protected Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}
}
