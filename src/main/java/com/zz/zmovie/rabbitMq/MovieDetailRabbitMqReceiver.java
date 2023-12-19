package com.zz.zmovie.rabbitMq;


import com.zz.zmovie.mapper.MovieDetailMapper;
import com.zz.zmovie.mapper.MovieMapper;
import com.zz.zmovie.po.Movie;
import com.zz.zmovie.po.MovieDetail;
import com.zz.zmovie.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RabbitListener(queues = "ZMovie_movie_detail")//监听的队列名称 TestDirectQueue
public class MovieDetailRabbitMqReceiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MovieDetailMapper movieDetailMapper;

    @Autowired
    private MovieMapper movieMapper;

    @RabbitHandler
    public void receiveMessage(String message){
        logger.info("电影详情修改请求："+message);
        
        MovieDetail movieDetail =  RedisService.stringToBean(message, MovieDetail.class);
        
        if(movieDetail.getId() == 0){
            System.out.println("新增");
//            int res = movieDetailMapper.insert(movieDetail);
//
//            Long ID= movieDetail.getId();
//
//            insertIntoMovie(ID);

        }else{
            System.out.println("修改");
            movieDetailMapper.updateById(movieDetail);
        }
    }

    //电影也要插入
    public int insertIntoMovie(Long ID){

        System.out.println("ID:"+ID);
        Movie movie = new Movie();
        movie.setMovieId(ID);
        movie.setScore(0);
        movie.setPutOnDate(new Date());

        return movieMapper.insert(movie);

    }
}
