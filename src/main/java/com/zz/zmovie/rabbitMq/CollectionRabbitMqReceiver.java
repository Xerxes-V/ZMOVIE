package com.zz.zmovie.rabbitMq;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zz.zmovie.mapper.CollectionsMapper;
import com.zz.zmovie.mapper.MovieMapper;
import com.zz.zmovie.po.Collections;
import com.zz.zmovie.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RabbitListener(queues = "ZMovie_collections")//监听的队列名称 TestDirectQueue
public class CollectionRabbitMqReceiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CollectionsMapper collectionsMapper;

    @Autowired
    private MovieMapper movieMapper;

    @RabbitHandler
    public void receiveMessage(String message){
        logger.info("用户修改请求："+message);

        //判断是 增 or 删：
        CollectionsAssist collectionsAssist = RedisService.stringToBean(message,CollectionsAssist.class);


           if(collectionsAssist.isCollected()){    //增    当唯一索引冲突时则取消插入操作
               collectionsMapper.insertCollection(collectionsAssist.getMovieId(),collectionsAssist.getUserId(),new Date());
               movieMapper.collect(collectionsAssist.getMovieId(),1);
           }else{
               QueryWrapper<Collections> queryWrapper = new QueryWrapper();
               queryWrapper.eq("user_id",collectionsAssist.getUserId())
                       .and(c->c.eq("movie_id",collectionsAssist.getMovieId()));
               collectionsMapper.delete(queryWrapper);
               movieMapper.collect(collectionsAssist.getMovieId(),-1);

           }

    }

}
