package com.zz.zmovie.redis;

import cn.hutool.core.util.TypeUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.zz.zmovie.exception.GlobleException;
import com.zz.zmovie.po.User;
import com.zz.zmovie.utils.ResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service

public class RedisService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisPoolFactory redisPoolFactory;      //引入连接池：


    /**
     * 从 redis 中取出数据
     * @param prefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(RedisPrefix prefix,String key,Class<T> clazz){
        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = prefix.getPrefix() + "_" + key;        //真正存储到 redis 中的键：

            String res = jedis.get(realKey);
            T t = stringToBean(res,clazz);      //将结果重新转换



            System.out.println("redis结果："+t);
            return t;
        } finally {
            jedis.close();
        }
    }

    /**
     * 插入
     * @param redisPrefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean set(RedisPrefix redisPrefix,String key,T value){
        JedisPool jedisPool = null;
        Jedis jedis = null;
//        jedis.set
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" +key;
            String val = beanToString(value);
            int expireSeconds = redisPrefix.getExpireSeconds();     //过期时间


            if(expireSeconds > 0){
                jedis.setex(realKey,expireSeconds,val);
            }else{
                jedis.set(realKey,val);
            }

            jedis.close();
            return true;
        }catch (Exception e){
            throw e;
        }finally {
            jedis.close();
        }

    }


    public boolean del(RedisPrefix redisPrefix,String key){
        logger.info(" redis 删除" + redisPrefix.getPrefix());

        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" +key;

            jedis.del(realKey);
            return true;
        } finally {
            jedis.close();
        }
    }


    /**
     * redis 设置 list
     * @param redisPrefix
     * @param key
     * @param value
     * @param <T>
     * @return
     */
    public <T> boolean setList(RedisPrefix redisPrefix,String key,List<T> value){
        String data = listToString(value);
        return set(redisPrefix,key,value);
    }

    public <T> List<T> getList(RedisPrefix prefix,String key,Class<T> clazz){
        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = prefix.getPrefix() + "_" + key;

            String res = jedis.get(realKey);

            if(StringUtils.isEmpty(res)){
                return null;
            }
            List<T> list = stringToList(res,clazz);

            return list;
        } finally {
            jedis.close();
        }
    }

    /**
     * 判断 redis 是否存在 key
     * @param redisPrefix
     * @param key
     * @return
     */
    public Boolean existKey(RedisPrefix redisPrefix,String key){
        logger.info("查询是否存在：" + redisPrefix.getPrefix() + key  );
        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" + key;
            Boolean res = jedis.exists(realKey);

            return res;

        } finally {
            jedis.close();
        }
    }

    public <T> Boolean update(RedisPrefix redisPrefix,String key,T value){
        logger.info("redis修改信息："+redisPrefix.getPrefix()+value);

        JedisPool jedisPool = null;
        Jedis jedis = null;
//        jedis.set
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" +key;
            String val = beanToString(value);
            int expireSeconds = Integer.parseInt(String.valueOf(jedis.ttl(realKey)));

            if(expireSeconds > 0){
                jedis.setex(realKey,expireSeconds,val);
            }else{
                jedis.set(realKey,val);
            }
            return true;
        } finally {
            jedis.close();
        }
    }

    //redis 初始化列表添加 set，
    public <T> Boolean setListByList(RedisPrefix redisPrefix,String key,List<T> list){
        logger.info("redis初始化 列表："+redisPrefix.getPrefix()+" : "+key);

        JedisPool jedisPool = null;
        Jedis jedis = null;
        jedisPool = redisPoolFactory.JedisPoolFactory();
        jedis= jedisPool.getResource();      //从池子中获取连接

        String realKey = redisPrefix.getPrefix() + "_" +key;
        int expireSeconds = redisPrefix.getExpireSeconds();
        Transaction multi = jedis.multi();           //通过事务进行初始化
        try {
            //2、命令入队
            for(int i = 0;i<list.size();i++){
                String val = beanToString(list.get(i));
                multi.lpush(realKey,val);
            }

            //过期时间
            multi.expire(realKey,expireSeconds);

             multi.exec();
            return true;
        } catch (Exception e) {
            //当出现错误，放弃事务
            System.out.println(e.toString());
            multi.discard();
            throw new GlobleException(ResultStatus.REDIS_SET_ADD_ERROR);
        } finally {
            jedis.close();
        }
    }

    //redis 添加 set
    public <T> Boolean setListRedis(RedisPrefix redisPrefix,String key,T value){
        logger.info("redis添加 列表："+key+" : "+value);

        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" +key;

            String val = beanToString(value);
            int expireSeconds = redisPrefix.getExpireSeconds();

            if(expireSeconds > 0 && jedis.ttl(realKey) < 0){        //当前没有这一个键则设置，有了就算了
                jedis.expire(realKey,expireSeconds);
            }
            jedis.lrem(realKey,Long.parseLong("1"),val);
            jedis.lpush(realKey,val);
            

            if(redisPrefix == UserPrefix.userHistory){
                System.out.println("???");
                jedis.ltrim(realKey,0,59);
            }

            return true;
        } finally {
            jedis.close();
        }
    }

    //redis 删除 set 元素
    public <T> Boolean delList(RedisPrefix redisPrefix,String key,T value){
        logger.info("redis删除 列表："+key+" : "+value);

        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" +key;

            String val = beanToString(value);

            jedis.lrem(realKey,Long.parseLong("1"),val);
            return true;
        } finally {
            jedis.close();
        }
    }

    //redis 获取列表：
    public <T> List<T> getListRedis(RedisPrefix redisPrefix,String key, Class<T> clazz){
        logger.info("redis获取 列表："+redisPrefix.getPrefix()+" : "+key);

        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" +key;

            List<String> list = jedis.lrange(realKey,0,-1);
            List<T> res = new ArrayList<>();

//            list.forEach(System.out::println);

            for (int i = 0; i < list.size(); i++) {
                res.add(stringToBean(list.get(i),clazz));
            }

            return res;
        } finally {
            jedis.close();
        }
    }


    //redis 获取 set 元素
    public <T> Set<String> getSetVals(RedisPrefix redisPrefix,String key ){
        logger.info("redis获取 set："+key+" : " );

        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" +key;

            Set<String> res =  jedis.smembers(realKey);
//            res.forEach(System.out::println);

            return res;
        } finally {
            jedis.close();
        }
    }


    public Long getListLen(RedisPrefix redisPrefix,String key){
        JedisPool jedisPool = null;
        Jedis jedis = null;
        try {
            jedisPool = redisPoolFactory.JedisPoolFactory();
            jedis= jedisPool.getResource();      //从池子中获取连接

            String realKey = redisPrefix.getPrefix() + "_" +key;

            Long len  = jedis.llen(realKey);

            return len;
        } finally {
            jedis.close();
        }
    }


    public static <T> T stringToBean(String str, Class<T> clazz) {
        if(str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if(clazz == int.class || clazz == Integer.class) {
            return (T)Integer.valueOf(str);
        }else if(clazz == String.class) {
            return (T)str;
        }else if(clazz == long.class || clazz == Long.class) {
            return  (T)Long.valueOf(str);
        }else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }


    public static <T> String beanToString(T value) {
        if(value == null) {
            return null;
        }

         Class<?> clazz = value.getClass();
        if(clazz == int.class || clazz == Integer.class) {
            return ""+value;
        }else if(clazz == String.class) {
            return (String)value;
        }else if(clazz == long.class || clazz == Long.class) {
            return ""+value;
        }else {
             return JSON.toJSONString(value);
        }
    }

    public static <T> String listToString(List<T> list){
        JSONArray jsonArray = JSONUtil.parseArray(list);
        return jsonArray.toString();
    }

    public static <T> List<T> stringToList(String str,Class<T> clazz){
        JSONArray jsonArray = JSONUtil.parseArray(str);

        List<T> list = JSONUtil.toList(jsonArray, clazz);
        return list;
    }

}
