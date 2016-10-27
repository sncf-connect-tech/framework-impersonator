package com.vsct.impersonator.http.record;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import com.google.common.base.Optional;
import com.vsct.impersonator.http.message.storage.IHttpExchangeStore;

/**
 * Cette classe est un enregistreur de requête HTML. Il démarre des qu'il est instancié et peut être arreté avec la
 * méthode stop().
 */
public class Recorder {
	public static final int DEFAULT_MAX_CONTENT_LENGTH = 10485760;

	private static final Logger LOGGER = Logger.getLogger(Recorder.class);

	private final Channel channel;

	/**
	 * Créer un nouveau recorder.
	 *
	 * @param name
	 *            Nom
	 * @param localport
	 *            port d'écoute du recorder
	 * @param targetHost
	 *            adresse cible des requêtes envoyées au recorder
	 * @param targetPort
	 *            port de l'adresse cible des requêtes envoyées au recorder
	 * @param exchangeStore
	 *            implémentation de IHttpExchangeStore permettant d'enregistrer les dialogues HTTP passant par le
	 *            recorder
	 * @param sslEnable
	 *            SSL activé ?
	 * @return Recorder
	 */
	public static Recorder create(final String name, final int localport, final String targetHost, final int targetPort,
			final IHttpExchangeStore exchangeStore, final boolean sslEnable) {
		final InetSocketAddress targetAddress = new InetSocketAddress(targetHost, targetPort);
		return create(name, localport, targetAddress, exchangeStore, sslEnable, Optional.<InetSocketAddress> absent());
	}

	/**
	 * Créer un nouveau recorder.
	 *
	 * @param name
	 *            Nom
	 * @param localport
	 *            port d'écoute du recorder
	 * @param targetHost
	 *            adresse cible des requêtes envoyées au recorder
	 * @param targetPort
	 *            port de l'adresse cible des requêtes envoyées au recorder
	 * @param exchangeStore
	 *            implémentation de IHttpExchangeStore permettant d'enregistrer les dialogues HTTP passant par le
	 *            recorder
	 * @param sslEnable
	 *            SSL activé ?
	 * @param contentLength
	 *            taille max d'une réponse HTTP
	 * @return Recorder
	 */
	public static Recorder create(final String name, final int localport, final String targetHost, final int targetPort,
			final IHttpExchangeStore exchangeStore, final boolean sslEnable, final int contentLength) {
		final InetSocketAddress targetAddress = new InetSocketAddress(targetHost, targetPort);
		return create(name, localport, targetAddress, exchangeStore, sslEnable, Optional.<InetSocketAddress> absent(), contentLength);
	}

	/**
	 * Créer un nouveau recorder avec user et password
	 *
	 * @param name
	 *            Nom
	 * @param localport
	 *            port d'écoute du recorder
	 * @param targetHost
	 *            adresse cible des requêtes envoyées au recorder
	 * @param targetPort
	 *            port de l'adresse cible des requêtes envoyées au recorder
	 * @param exchangeStore
	 *            implémentation de IHttpExchangeStore permettant d'enregistrer les dialogues HTTP passant par le
	 *            recorder
	 * @param sslEnable
	 *            SSL activé ?
	 * @param contentLength
	 *            taille max d'une réponse HTTP
	 * @return Recorder
	 */
	public static Recorder create(final String name, final int localport, final String targetHost, final int targetPort,
			final IHttpExchangeStore exchangeStore, final boolean sslEnable, final int contentLength, final String user,
			final String password) {
		final InetSocketAddress targetAddress = new InetSocketAddress(targetHost, targetPort);
		return create(name, localport, targetAddress, exchangeStore, sslEnable, Optional.<InetSocketAddress> absent(), contentLength, user,
				password);
	}

	/**
	 * Créer un nouveau recorder avec InetSocketAddress
	 *
	 * @param name
	 *            Nom
	 * @param localport
	 *            port d'écoute du recorder
	 * @param targetAddress
	 *            adresse cible des requêtes envoyées au recorder
	 * @param exchangeStore
	 *            implémentation de IHttpExchangeStore permettant d'enregistrer les dialogues HTTP passant par le
	 *            recorder
	 * @param sslEnable
	 *            SSL activé ?
	 * @param proxyAddress
	 *            Adresse cible du proxy (optionelle)
	 * @return Recorder
	 */
	public static Recorder create(final String name, final int localport, final InetSocketAddress targetAddress,
			final IHttpExchangeStore exchangeStore, final boolean sslEnable, final Optional<InetSocketAddress> proxyAddress) {
		return create(name, localport, targetAddress, exchangeStore, sslEnable, proxyAddress, DEFAULT_MAX_CONTENT_LENGTH);
	}

	/**
	 * Créer un nouveau recorder avec InetSocketAddress et login + password
	 *
	 * @param name
	 *            Nom
	 * @param localport
	 *            port d'écoute du recorder
	 * @param targetAddress
	 *            adresse cible des requêtes envoyées au recorder
	 * @param exchangeStore
	 *            implémentation de IHttpExchangeStore permettant d'enregistrer les dialogues HTTP passant par le
	 *            recorder
	 * @param sslEnable
	 *            SSL activé ?
	 * @param proxyAddress
	 *            Adresse cible du proxy (optionelle)
	 * @param user
	 * @param password
	 * @return
	 */
	public static Recorder create(final String name, final int localport, final InetSocketAddress targetAddress,
			final IHttpExchangeStore exchangeStore, final boolean sslEnable, final Optional<InetSocketAddress> proxyAddress,
			final String user, final String password) {
		return create(name, localport, targetAddress, exchangeStore, sslEnable, proxyAddress, DEFAULT_MAX_CONTENT_LENGTH, user, password);
	}

	/**
	 * Créer un nouveau recorder.
	 *
	 * @param name
	 *            Nom
	 * @param localport
	 *            port d'écoute du recorder
	 * @param targetAddress
	 *            adresse cible des requêtes envoyées au recorder
	 * @param exchangeStore
	 *            implémentation de IHttpExchangeStore permettant d'enregistrer les dialogues HTTP passant par le
	 *            recorder
	 * @param sslEnable
	 *            SSL activé ?
	 * @param proxyAddress
	 *            Adresse cible du proxy (optionelle)
	 * @param maxContentLength
	 *            Taille max du contenu d'une réponse HTTP (par défaut {@value #DEFAULT_MAX_CONTENT_LENGTH}
	 * @return Recorder
	 */
	public static Recorder create(final String name, final int localport, final InetSocketAddress targetAddress,
			final IHttpExchangeStore exchangeStore, final boolean sslEnable, final Optional<InetSocketAddress> proxyAddress,
			final int maxContentLength) {
		return new Recorder(name, localport, targetAddress, exchangeStore, sslEnable, proxyAddress, maxContentLength, null, null);
	}

	public static Recorder create(final String name, final int localport, final InetSocketAddress targetAddress,
			final IHttpExchangeStore exchangeStore, final boolean sslEnable, final Optional<InetSocketAddress> proxyAddress,
			final int maxContentLength, final String user, final String password) {
		return new Recorder(name, localport, targetAddress, exchangeStore, sslEnable, proxyAddress, maxContentLength, user, password);
	}

	private Recorder(final String name, final int localport, final InetSocketAddress serverAddress, final IHttpExchangeStore exchangeStore,
			final boolean sslEnable, final Optional<InetSocketAddress> proxyAddress, final int maxContentLength, final String user,
			final String password) {

		ExecutorService threadPool = Executors.newCachedThreadPool();
		ServerChannelFactory serverChannelFactory = new OioServerSocketChannelFactory(threadPool, threadPool);
		ServerBootstrap bootstrap = new ServerBootstrap(serverChannelFactory);
		ClientSocketChannelFactory clientChannelFactory = new OioClientSocketChannelFactory(threadPool);
		bootstrap.setPipelineFactory(new RecorderPipelineFactory(clientChannelFactory, exchangeStore, serverAddress, sslEnable,
				proxyAddress, maxContentLength, user, password));

		// Ouverture d'un chanel en écoute
		channel = bootstrap.bind(new InetSocketAddress(localport));
		LOGGER.info("Server listening on : " + name + " " + channel.getLocalAddress());
	}

	public void stop() {
		channel.close().awaitUninterruptibly();
	}

}
