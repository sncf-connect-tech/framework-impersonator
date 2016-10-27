package com.vsct.impersonator.http.message.storage;

import java.util.List;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.vsct.impersonator.http.message.HttpExchange;

public interface IHttpExchangeLoader {

	HttpResponse loadResponseFor(HttpRequest request);

	List<HttpExchange> all();

}