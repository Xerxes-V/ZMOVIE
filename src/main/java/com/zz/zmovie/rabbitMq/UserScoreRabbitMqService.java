package com.zz.zmovie.rabbitMq;


import com.zz.zmovie.mapper.MovieMapper;
import com.zz.zmovie.mapper.UserScoredMapper;
import com.zz.zmovie.po.Movie;
import com.zz.zmovie.po.UserScored;
import com.zz.zmovie.redis.MoviePrefix;
import com.zz.zmovie.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "ZMovie_userScore")//监听的队列名称 TestDirectQueue
public class UserScoreRabbitMqService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserScoredMapper userScoredMapper;

    @Autowired
    private MovieMapper movieMapper;

    @Autowired
    private RedisService redisService;


    @RabbitHandler
    public void receiveMessage(String message){
        logger.info("用户修改请求："+message);

        UserScored userScored = RedisService.stringToBean(message,UserScored.class);

        Long movie_id = userScored.getMovieId();


        //先新增到用户评分：
        userScoredMapper.updateScore(userScored.getMovieId(),userScored.getUserId(),userScored.getScore());

        //判断是 修改分数 or 新增评分：
        System.out.println("or :"+userScored);

        Movie movie = new Movie();

        if(userScored.getId() < 0){
            movieMapper.updateScore(userScored.getMovieId(),userScored.getScore());
        }else{ //新增
            movieMapper.addScore(userScored.getMovieId(),userScored.getScore());
        }

        redisService.del(MoviePrefix.getMovieDetail,String.valueOf(userScored.getMovieId()));
    }


}
