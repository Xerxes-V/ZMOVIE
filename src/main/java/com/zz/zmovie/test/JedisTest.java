package com.zz.zmovie.test;


import com.alibaba.fastjson.JSONObject;
import com.zz.zmovie.redis.RedisConfig;
import com.zz.zmovie.redis.RedisPoolFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


public class JedisTest {

    @Autowired
    RedisPoolFactory rf;


    @Test
    public void test(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxTotal(100);

//        JedisPool jedisPool = new JedisPool(jedisPoolConfig,"47.107.31.36",6379,1000,"010507",1);

//        RedisPoolFactory rf = new RedisPoolFactory();
        JedisPool jedisPool = rf.JedisPoolFactory();

        Jedis jedis = jedisPool.getResource();

        jedis.ping();

        System.out.println(jedis.ping());



        jedis.close();

    }
}
