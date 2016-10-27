package com.vsct.impersonator.http.message.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.google.common.io.CharStreams;
import com.vsct.impersonator.http.message.HttpExchange;

/**
 * Implémentation spécifique de IHttpExchangeLoader permettant de renvoyer une réponse par défaut si
 * defaultExchangeLoader n'a pas trouvé de réponse
 */
public class DefaultResponseExchangeLoader implements IHttpExchangeLoader {
	private static final Logger LOGGER = Logger.getLogger(DefaultResponseExchangeLoader.class);

	private final IHttpExchangeLoader defaultExchangeLoader;

	private final HttpExchangeSerialiser serialiser;

	private final String defaultResponseName;

	public DefaultResponseExchangeLoader(final IHttpExchangeLoader defaultExchangeLoader, final HttpExchangeSerialiser serialiser,
			final String defaultResponseName) {
		this.defaultExchangeLoader = defaultExchangeLoader;
		this.serialiser = serialiser;
		this.defaultResponseName = defaultResponseName;
	}

	@Override
	public HttpResponse loadResponseFor(final HttpRequest request) {
		HttpResponse response = defaultExchangeLoader.loadResponseFor(request);
		if (response.getStatus().equals(HttpResponseStatus.NOT_FOUND)) {
			LOGGER.debug("Réponse inconnue, renvoie de la réponse par défaut : " + defaultResponseName);
			// On récupére la réponse par defaut dans le classePath
			try (InputStream resource = this.getClass().getResourceAsStream(defaultResponseName)) {
				return serialiser.deserialiseResponse(CharStreams.toString(new InputStreamReader(resource)));
			} catch (IOException e) {
				// On log et on en fait rien, la réponse NOT_FOUND sera envoyé
				LOGGER.error("Erreur lors de la lecture de la réponse par défaut", e);
			}
		}

		return response;
	}

	@Override
	public List<HttpExchange> all() {
		return defaultExchangeLoader.all();
	}

}
