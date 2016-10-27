package com.vsct.impersonator.http.message.storage;

public class RedisConfiguration {
	private final String host;
	private final int port;
	private final String password;
	private final String entity;

	public RedisConfiguration(final String host, final int port, final String password, final String entity) {
		this.host = host;
		this.port = port;
		this.password = password;
		this.entity = entity;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getPassword() {
		return password;
	}

	public String getEntity() {
		return entity;
	}

}
