package com.example.mail.Redis;

//import com.example.mail.jedisUtils.JedisUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis工具类
 * 步骤： 建立连接 -> 调用实现接口: Thead抢占模型 -》 测试 -》结束：关闭。
 */
public class RedisUtil {
    protected static Logger logger = LoggerFactory.getLogger(RedisUtil.class);
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 6379;

    private static JedisPool jedisPool = null;

    private RedisUtil() {

    }

    /**
     * 初始化JedisPool
     */
    private static void initialPool() {
        if (jedisPool == null) {
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            //指定连接池中最大的空闲链接数
            jedisPoolConfig.setMaxIdle(100);
            //连接池创建的最大连接数
            jedisPoolConfig.setMaxTotal(500);
            //设置创建连接的超时时间
            jedisPoolConfig.setMaxWaitMillis(1000 * 500);
            //表示从连接池中获取连接时，先测试连接是否能使用
            jedisPoolConfig.setTestOnBorrow(true);
            jedisPool = new JedisPool(jedisPoolConfig, HOST, PORT);
        }
    }
    /**
     * 在多线程环境同步初始化
     */
    protected static  synchronized void poolInit() {
        if(jedisPool == null) {
            initialPool();
        }
    }

    /**
     * 同步获取Jedis实例
     * @return
     */
    public synchronized static Jedis getJedis() {
        if (jedisPool == null) {
            poolInit();
        }
        Jedis jedis = null;
        try {
            if (jedisPool != null) {
                jedis = jedisPool.getResource();
            }
        } catch (Exception e) {
            logger.error("获取jedis出错: " + e);
        } finally {
            returnResource(jedis);
        }
        return jedis;
    }
    /**
     * 一个转化
     */
    public synchronized static  JedisPool getJedisPool() {
        if (jedisPool == null) {
            poolInit();
        }
        return jedisPool;
    }


    /**
     * 释放jedis资源
     */
    public static void returnResource(Jedis jedis){
        if(jedis != null && jedisPool != null) {
            // Jedis3.0之后，returnResource遭弃用，官方重写了close方法
            // jedisPool.returnResource(jedis);
            jedis.close();
        }
    }

    /**
     * 释放jedis资源
     */
    public static void returnBrokenJedis(Jedis jedis) {
        if(jedis != null && jedisPool != null) {
            jedisPool.returnBrokenResource(jedis);
        }
        jedis = null;
    }

}
