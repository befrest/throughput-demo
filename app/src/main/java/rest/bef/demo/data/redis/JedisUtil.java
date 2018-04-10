package rest.bef.demo.data.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import rest.bef.demo.util.ConfigUtil;
import rest.bef.demo.util.EncryptUtil;
import rest.bef.demo.util.FileUtil;

public class JedisUtil {

    private static JedisPool jmPool;

    private static final String KEYSPACE_JM = "jm";

    static {
        setupJmPool();
    }

    private synchronized static void setupJmPool() {
        jmPool = setupPool(jmPool, KEYSPACE_JM);
    }

    private synchronized static JedisPool setupPool(JedisPool pool, String keySpace) {
        if (pool != null)
            return pool;

        return buildPool(keySpace);
    }

    private static JedisPool buildPool(String keySpace) {
        JedisPoolConfig conf = new JedisPoolConfig();
        conf.setBlockWhenExhausted(ConfigUtil.getBoolean(String.format("%s.redis.pool.blockWhenExhausted", keySpace)));
        conf.setMaxIdle(ConfigUtil.getInt(String.format("%s.redis.pool.maxIdle", keySpace)));
        conf.setMaxTotal(ConfigUtil.getInt(String.format("%s.redis.pool.max", keySpace)));
        conf.setTestWhileIdle(ConfigUtil.getBoolean(String.format("%s.redis.pool.testWhileIdle", keySpace)));
        conf.setTestOnReturn(ConfigUtil.getBoolean(String.format("%s.redis.pool.testOnReturn", keySpace)));
        conf.setTestOnBorrow(ConfigUtil.getBoolean(String.format("%s.redis.pool.testOnBorrow", keySpace)));

        return new JedisPool(
                conf,
                ConfigUtil.get(String.format("%s.redis.host", keySpace)),
                ConfigUtil.getInt(String.format("%s.redis.port", keySpace)));
    }

    public static Jedis getJmJedis() {
        return jmPool.getResource();
    }

    public static String loadScriptByFile(Jedis j, String file) {
        String script = FileUtil.getFileContent(file);
        String hash = EncryptUtil.sha1(script).toLowerCase();
        Boolean exists = j.scriptExists(hash);

        return (exists != null && exists)
                ? hash
                : j.scriptLoad(script);
    }
}
