package com.vsct.impersonator.http.message.storage;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

public abstract class AbstractHttpRedisStorage {

	protected Logger log = Logger.getLogger(getClass());
	protected HttpExchangeSerialiser serialiser;
	protected JedisPool jedisPool;
	protected String entity;

	private static final int TIMEOUT = 1000;

	public AbstractHttpRedisStorage(final RedisConfiguration config, final HttpExchangeSerialiser serialiser) {
		this.serialiser = serialiser;
		this.entity = config.getEntity();
		if (StringUtils.isNotEmpty(config.getPassword())) {
			this.jedisPool = new JedisPool(new JedisPoolConfig(), config.getHost(), config.getPort(), TIMEOUT, config.getPassword());
		} else {
			this.jedisPool = new JedisPool(new JedisPoolConfig(), config.getHost(), config.getPort(), TIMEOUT);
		}
	}

	/**
	 * Enregistrement dans Redis
	 * 
	 * @param key
	 * @param contents
	 */
    protected void save(final String key, final String contents) {
        Jedis jedis = null;
        boolean resourceAlreadyClosed = false;
        try {
            jedis = jedisPool.getResource();
            jedis.hset(entity, key, contents);
        } catch (JedisDataException e) {
            log.error("Erreur d'écriture dans Redis, vérifiez le mot de passe dans la configuration.\nDetails : " + e.getMessage());
        } catch (JedisConnectionException e) {
            jedisPool.returnBrokenResource(jedis);
            resourceAlreadyClosed = true;
            log.error("Erreur d'écriture dans Redis.\nDetails : " + e.getMessage());
        } finally {
            if (!resourceAlreadyClosed) {
                jedisPool.returnResource(jedis);
            }
        }
    }

	/**
	 * Récupération de la valeur associée à la clé dans Redis
	 * 
	 * @param key
	 * @return
	 */
    protected String retrieve(final String key) {
        Jedis jedis = null;
        boolean resourceAlreadyClosed = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.hget(entity, key);
        } catch (JedisDataException e) {
            log.error("Erreur de la lecture dans Redis, vérifiez le mot de passe dans la configuration.\nDetails : " + e.getMessage());
        } catch (JedisConnectionException e) {
            jedisPool.returnBrokenResource(jedis);
            resourceAlreadyClosed = true;
            log.error("Erreur de la lecture dans Redis.\nDetails : " + e.getMessage());
        } finally {
            if (!resourceAlreadyClosed) {
                jedisPool.returnResource(jedis);
            }
        }
        return null;
    }

	/**
	 * Récupération de toutes les clés suffixé par .request
	 * 
	 * @return
	 */
    protected Set<String> getAllKeys() {
        Jedis jedis = null;
        boolean resourceAlreadyClosed = false;
        try {
            jedis = jedisPool.getResource();
            return jedis.hkeys("*.request");
        } catch (JedisDataException e) {
            log.error("Erreur de la lecture dans Redis, vérifiez le mot de passe dans la configuration.\nDetails : " + e.getMessage());
        } catch (JedisConnectionException e) {
            jedisPool.returnBrokenResource(jedis);
            resourceAlreadyClosed = true;
            log.error("Erreur de la lecture dans Redis.\nDetails : " + e.getMessage());
        } finally {
            if (!resourceAlreadyClosed) {
                jedisPool.returnResource(jedis);
            }
        }
        return null;
    }

	protected HttpResponse notFound() {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	}

}