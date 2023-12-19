package com.zz.zmovie.rabbitMq;


import com.zz.zmovie.mapper.MovieMapper;
import com.zz.zmovie.po.Movie;
import com.zz.zmovie.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "ZMovie_movie")//监听的队列名称 TestDirectQueue
public class MovieRabbitMqReceiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MovieMapper movieMapper;

    @RabbitHandler
    public void receiveMessage(String message){
        logger.info("用户修改请求："+message);

        Movie movie = RedisService.stringToBean(message, Movie.class);
        movieMapper.updateById(movie);
    }

}
