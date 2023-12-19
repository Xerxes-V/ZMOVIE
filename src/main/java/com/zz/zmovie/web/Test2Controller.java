package com.zz.zmovie.web;

import com.zz.zmovie.exception.GlobleException;
import com.zz.zmovie.mapper.TestMapper;
import com.zz.zmovie.redis.RedisPoolFactory;
import com.zz.zmovie.service.RecommendationService;
import com.zz.zmovie.utils.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@CrossOrigin
@RequestMapping("/webTest")
public class Test2Controller {

    @Autowired
    private TestMapper testMapper;

    @Autowired
    private RedisPoolFactory redisPoolFactory;

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/testmbp")
    public String mbpTest(){
        testMapper.selectList(null);
//        Te st te = testMapper.selectById(1);

        JedisPool jedisPool = redisPoolFactory.JedisPoolFactory();
        Jedis jedis = jedisPool.getResource();
        String res = jedis.ping();
        jedis.set("key","val");
        jedis.close();
        return res;
    }

    @GetMapping("/t")
    public void test(){
        int a = 1/0;
        throw new GlobleException(ResultStatus.SUCCESS);

    }


    @GetMapping("test")
    public void t(){
        recommendationService.getRecommends(1631710854995791874l);
    }
}
