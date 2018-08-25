package me.raauhh.uhc.manager.redis;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Getter
public final class RedisWrapper {

    private final JedisPool pool;

    public RedisWrapper(ConfigurationSection configuration) {
        String[] split = configuration.getString("address", "localhost").split(":");
        String host = split[0];

        int port = split.length > 1 ? Integer.parseInt(split[1]) : 6379;
        if (configuration.getBoolean("authentication.enabled", false))
            pool = new JedisPool(new JedisPoolConfig(), host, port, 2000, configuration.getString("authentication.password"));
        else
            pool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    public Jedis getJedis() {
        return pool.getResource();
    }

    public void close() {
        if (pool != null) {
            pool.close();
        }
    }
}