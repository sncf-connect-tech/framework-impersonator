package com.vsct.impersonator.http.message.storage;

import com.vsct.impersonator.http.message.HttpExchange;

public interface IHttpExchangeStore {

	void store(HttpExchange exchange);

}