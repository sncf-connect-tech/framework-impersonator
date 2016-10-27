package com.vsct.impersonator.http.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Classe contenant un compteur de requête et un compteur d'erreur.
 */
public class QueryCounter {
	private final AtomicInteger query = new AtomicInteger(0);

	private final AtomicInteger error = new AtomicInteger(0);

	public void add() {
		query.incrementAndGet();
	}

	public void error() {
		error.incrementAndGet();
	}

	public int getQuery() {
		return query.get();
	}

	public int getError() {
		return error.get();
	}
}
