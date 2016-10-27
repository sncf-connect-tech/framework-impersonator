package com.vsct.impersonator.http.util;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.log4j.Logger;

/**
 * Cette classe permet d'attendre pendant un temps alléatoire compris entre timeMin et timeMax (en milliseconde)
 */
public class Sleeper {
	private static final Logger LOGGER = Logger.getLogger(Sleeper.class);

	private final RandomDataGenerator randomData = new RandomDataGenerator();

	private final Bounds bounds;

	public Sleeper(final Bounds bounds) {
		this.bounds = bounds;
	}

	public void sleep() {
		int timeMin = bounds.getMin();
		int timeMax = bounds.getMax();

		int wait = (timeMax > timeMin) ? randomData.nextInt(timeMin, timeMax) : timeMin;
		LOGGER.debug("activity simulation waiting time: " + wait + "ms");
		try {
			if (wait > 0) {
				Thread.sleep(wait);
			}
		} catch (InterruptedException e) {
			LOGGER.warn("activity simulation waiting time interrupted: " + e.getMessage(), e);
		}
	}

	public interface Bounds {
		int getMin();

		int getMax();
	}

	/**
	 * Met le thread en pause pour un temps aléatoire, en prenant en compte le
	 * temps de traitement déjà éffectué, représenté par le paramètre startTime
	 * de la méthodz
	 */
	public void sleep(Long startTime) {
		
		int timeMin = bounds.getMin();
		int timeMax = bounds.getMax();
		
		Long currentTime = System.currentTimeMillis();
		int wait = 0;
		
		try {
			wait = (timeMax > timeMin) ? randomData.nextInt(timeMin - (int) (startTime - currentTime), timeMax) : timeMin;
			LOGGER.debug("activity simulation waiting time: " + wait + "ms");
		} catch (Exception ex) {
			LOGGER.warn("unable to calcultate waiting time : " + ex.getMessage(), ex);
		}
		
		try {
			if (wait > 0) {
				Thread.sleep(wait);
			}
		} catch (InterruptedException e) {
			LOGGER.warn("activity simulation waiting time interrupted: " + e.getMessage(), e);
		}		
	}
}
