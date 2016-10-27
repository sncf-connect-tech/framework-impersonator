package com.vsct.impersonator.http.message;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class HttpExchange {
    private final HttpRequest request;
    private final HttpResponse response;

    public HttpExchange(HttpRequest httpRequest, HttpResponse httpResponse) {
        request = httpRequest;
        response = httpResponse;
    }

    public HttpRequest request() {
        return request;
    }

    public HttpResponse response() {
        return response;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((request == null) ? 0 : request.hashCode());
        result = prime * result + ((response == null) ? 0 : response.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HttpExchange other = (HttpExchange) obj;
        if (request == null) {
            if (other.request != null) {
                return false;
            }
        } else if (!request.equals(other.request)) {
            return false;
        }
        if (response == null) {
            if (other.response != null) {
                return false;
            }
        } else if (!response.equals(other.response)) {
            return false;
        }
        return true;
    }

}