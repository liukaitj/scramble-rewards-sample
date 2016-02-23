package lk.lab.rewards.redis;

import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisHelper {
	
	private static JedisPool pool;
	private static ThreadLocal<Jedis> jedisInThread = new ThreadLocal<Jedis>();
	
	public static Jedis getJedisInThread() {
		if (jedisInThread.get() == null) {
			Jedis jedis = getJedisFromPool();
			jedisInThread.set(jedis);
			return jedis;
		}
		
		return jedisInThread.get();
	}
	
	public static JedisPool getPool() {
		if (pool == null) {
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxTotal(100);
			config.setMaxIdle(5);
			config.setMaxWaitMillis(100000L);
			config.setTestOnBorrow(true);
			pool = new JedisPool(config, "192.168.100.85", 6379);
		}
		return pool;
	}
	
	public static Jedis getJedisFromPool() {
		return getPool().getResource();
	}
	
	public static void returnJedisToPool() {
		Jedis jedis = getJedisInThread();
		jedis.close();
	}
	
	public static Set<String> keys(final String pattern) {
		Jedis jedis = getJedisFromPool();
		
		try {
			return jedis.keys(pattern);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			jedis.close();
		}
	}
	
	public static Set<String> zrange(final String key, final long start, final long end) {
		Jedis jedis = getJedisFromPool();
		
		try {
			return jedis.zrange(key, start, end);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			jedis.close();
		}
	}
	
	public static Long zrem(final String key, final String... members) {
		Jedis jedis = getJedisFromPool();
		
		try {
			return jedis.zrem(key, members);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			jedis.close();
		}
	}
	
	public static String set(final String key, String value) {
		Jedis jedis = getJedisFromPool();
		
		try {
			return jedis.set(key, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			jedis.close();
		}
	}

}
