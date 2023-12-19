package com.zz.zmovie.config;

import com.zz.zmovie.mapper.UserScoredMapper;
import com.zz.zmovie.service.RecommendationService;
import com.zz.zmovie.service.impl.RecommendationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class RecommendationConfig {
    @Autowired
    private UserScoredMapper userScoredMapper;

    @Autowired
    private RecommendationService recommendationService;


    //3.添加定时任务
    @Scheduled(cron = " 0 0 0/12 * * ?")
//    @Scheduled(cron = "0/5 * * * * ?")
    private void configureTasks() {
        System.err.println("执行推荐定时任务时间: " + LocalDateTime.now());
        List<Long> users = userScoredMapper.getMuchRatingsUser();
        for(long l : users){
            recommendationService.getRecommends(l);
        }
    }

}
