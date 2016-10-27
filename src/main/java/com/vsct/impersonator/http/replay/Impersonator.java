package com.vsct.impersonator.http.replay;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import com.vsct.impersonator.http.message.storage.IHttpExchangeLoader;
import com.vsct.impersonator.http.util.QueryCounter;
import com.vsct.impersonator.http.util.Sleeper;
import com.vsct.impersonator.http.util.Sleeper.Bounds;

/**
 * Cette classe est un impersonator de dialogue HTML. Il répond au requête qu'il reçoi en recherchant une réponse dans
 * celles préalablement enregistré avec un Recorder. Il démarre des qu'il est instancié et peut être arreté avec la
 * méthode stop().
 * 
 * La méthode getQueryCounter peut être utilisé pour récupérer un QueryCounter qui permet de récupérer le nombre de
 * requête total et le nombre de réponse en erreur.
 * 
 * Le QueryCounter peut facilement être exposé via JMX pour pouvoir accéder au nombre de requêtes traitées pendant le
 * fonctionnement de l'Impersonator
 * 
 * Exemple :
 * 
 * <pre>
 * public interface RequestCountersMBean {
 * 	int getQueryCount();
 * }
 * 
 * MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
 * ObjectName countersMBeanName = new ObjectName(&quot;com.vsct.wdi.mock:type=Counters&quot;);
 * 
 * RequestCountersMBean mBean = new RequestCountersMBean() {
 * 	public int getQueryCount() {
 * 		impersonator.getQueryCounter().getQuery();
 * 	}
 * }
 * 
 * mbs.registerMBean(mBean, countersMBeanName);
 * </pre>
 */
public class Impersonator {
	public static final int DEFAULT_MAX_CONTENT_LENGTH = 10485760;

	private static final Logger LOGGER = Logger.getLogger(Impersonator.class);

	private final Channel channel;
	private final QueryCounter queryCounter = new QueryCounter();

	/**
	 * 
	 * @param localport
	 *            port d'écoute de l'impersonator
	 * @param loader
	 *            implémentation
	 * @param bounds
	 * @return
	 */
	public static Impersonator create(final int localport, final IHttpExchangeLoader loader, final Bounds bounds) {
		return create(localport, loader, bounds, DEFAULT_MAX_CONTENT_LENGTH);
	}

	/**
	 * 
	 * @param localport
	 *            port d'écoute de l'impersonator
	 * @param loader
	 *            implémentation
	 * @param bounds
	 * @return
	 */
	public static Impersonator create(final int localport, final IHttpExchangeLoader loader, final Bounds bounds, final int maxContentLength) {
		return new Impersonator(localport, loader, bounds, maxContentLength);
	}

	private Impersonator(final int localport, final IHttpExchangeLoader loader, final Bounds bounds, final int maxContentLength) {
		ExecutorService threadPool = Executors.newCachedThreadPool();

		ChannelFactory channelFactory = new OioServerSocketChannelFactory(threadPool, threadPool);
		ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
		bootstrap.setPipelineFactory(new ImpersonatorPipelineFactory(loader, new Sleeper(bounds), queryCounter, maxContentLength));

		// Ouverture d'un chanel en écoute
		channel = bootstrap.bind(new InetSocketAddress(localport));
		LOGGER.info("Server listening on : " + channel.getLocalAddress());
	}

	public void stop() {
		channel.close().awaitUninterruptibly();
	}

	public QueryCounter getQueryCounter() {
		return queryCounter;
	}
}
