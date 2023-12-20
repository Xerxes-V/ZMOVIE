package com.zz.zmovie.redis;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Service
public class RedisPoolFactory {

    @Autowired
    private RedisConfig redisConfig;

    @Bean
    public JedisPool JedisPoolFactory() {
        //从 redisconfig 中获取值设置 线程池
        JedisPoolConfig poolConfig = new JedisPoolConfig();


        poolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());		//最大空闲

        poolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());		///最大连接数

        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait() * 1000);		//用尽后最大等待时间

        JedisPool jp = new JedisPool(poolConfig, redisConfig.getHost(), redisConfig.getPort(),
                redisConfig.getTimeout()*1000, redisConfig.getPassword(), 1);       //1号数据库

        return jp;
    }

}


