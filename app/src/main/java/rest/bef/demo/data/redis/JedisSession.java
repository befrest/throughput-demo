package rest.bef.demo.data.redis;

import redis.clients.jedis.Jedis;

public class JedisSession {

    private static final ThreadLocal<Jedis> qmJedis = new ThreadLocal<>();

    public static Jedis get() {
        return getJedis(qmJedis, JedisInstance.JOB_MANAGER);
    }

    public static void closeSession() {
        closeResource(qmJedis);
    }

    private static Jedis getJedis(ThreadLocal<Jedis> j, JedisInstance instance) {
        if (j.get() == null || !j.get().isConnected()) {
            switch (instance) {
                case JOB_MANAGER:
                    j.set(JedisUtil.getJmJedis());
                    break;
            }
        }

        return j.get();
    }

    private static void closeResource(ThreadLocal<Jedis> j) {
        if (j.get() != null && j.get().isConnected()) {
            j.get().close();
            j.set(null);
        }
    }

    private enum JedisInstance {
        ANALYTICS,
        JOB_MANAGER,
    }
}
