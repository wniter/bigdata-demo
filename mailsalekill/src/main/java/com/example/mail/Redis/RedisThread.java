package com.example.mail.Redis;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

import javax.xml.crypto.Data;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

/**
 * 用线程池模拟1w个调用
 */
public class RedisThread extends Thread {
    /**
     * 开始抢购时间
     */
    private static long start = 0;
    /**
     * 多长时间抢购完
     */
    private static long time = 0;
    /**
     * 抢到商品的数量
     */
    private static int count = 0;

    private JedisPool jedisPool;
    /**
     * 需要购买的商品
     */
    private String pro;

    public RedisThread(String pro, JedisPool jedisPool) {
        this.pro = pro;
        this.jedisPool = jedisPool;
    }

    /**
     * 打印成功抢购信息
     */
    private synchronized void print(String str) {
        //获取程序所在的目录。
        Class clazz = RedisMS.class;
        URL url = clazz.getResource("/");
        String path = url.toString();
        path = path.substring(6);

        //缓冲写出流
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(path + "/resultRedis.txt", true));
            bw.write(str);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 线程开始定时器
     */
    private long clock() {
//        String clock = "2016-11-18 20:47:00";
        //时间
        String clock = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        try {
            date = simpleDateFormat.parse(clock);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long time = date.getTime();

        return time;
    }

    @Override
    public void run() {
        //获取当前时间
        long currentTimeMills = System.currentTimeMillis();

        long mills = clock() - currentTimeMills;
        if (mills > 0) {
            try {
                Thread.sleep(mills);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (true) {
            //获取连接
            Jedis jedis = jedisPool.getResource();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                //获取商品apple的数量
                int proNum = Integer.parseInt(jedis.get(pro));
                List<Object> result = null;
                //如果还有库存
                if (proNum > 0) {
                    //监听商品pro
                    jedis.watch(pro);
                    int proNum1 = Integer.parseInt(jedis.get(pro));

                    if (proNum1 < proNum) {
                        jedis.unwatch();
                    } else {
                        //jedis方法开始事务
                        Transaction transaction = jedis.multi();

                        //购买商品。然后更改库存
                        transaction.set(pro, String.valueOf(proNum - 1));

                        //提交事务
                        result = transaction.exec();
                    }
                    //监听的商品被别的线程操作，则无法购买商品，需要进行排队操作，自己不修改商品的数量
                    if (result == null || result.isEmpty()) {
                        System.out.println(Thread.currentThread().getName() + "\t正在排队抢购\t" + pro + "....");
                    } else {
                        count++;

                        switch (count) {
                            case 1:
                                start = System.currentTimeMillis();
                                break;

                            case 50:
                                time = System.currentTimeMillis() - start;
                                System.out.println("========" + time);
                                break;
                            default:
                                break;
                        }
                        String str = Thread.currentThread().getName() + "\t抢购成功，商品名为：\t" + pro + "\t抢购时间：" + new Date().getTime();
                        System.out.println(str);
                        //把抢购成功的名单打印出去
                        print(str);
                    }

                } else {
                    //当库存为0时
                    System.out.println(pro + "已经卖完");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                RedisUtil.returnResource(jedis);
            } finally {
                RedisUtil.returnResource(jedis);
            }


        }

    }
}
