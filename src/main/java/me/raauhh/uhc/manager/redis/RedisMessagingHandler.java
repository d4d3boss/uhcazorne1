package me.raauhh.uhc.manager.redis;

import me.raauhh.uhc.UHC;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class RedisMessagingHandler {

    private JedisPubSub subscriber;

    public void unsubscribe() {
        UHC.getInstance().getLogger().info("Closing Redis messaging service...");
        subscriber.unsubscribe();
    }

    public void sendMessage(String channel, String message) {
        UHC.getInstance().getServer().getScheduler().runTaskAsynchronously(UHC.getInstance(), () -> {
            Jedis jedis = null;
            try {
                jedis = UHC.getInstance().getRedisWrapper().getJedis();
                jedis.publish(channel, message);
            } finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        });
    }

}
