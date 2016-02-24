package lk.lab.rewards.redis;

import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisHelper {

	private static JedisPool pool;

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

	public static void returnJedisToPool(Jedis jedis) {
		jedis.close();
	}

	public static Set<String> keys(Jedis jedis, final String pattern) {
		return jedis.keys(pattern);
	}

	public static Set<String> zrange(Jedis jedis, final String key, final long start,
			final long end) {
		return jedis.zrange(key, start, end);
	}

	public static Long zrem(Jedis jedis, final String key, final String... members) {
		return jedis.zrem(key, members);
	}

	public static String set(Jedis jedis, final String key, String value) {
		return jedis.set(key, value);
	}

}
