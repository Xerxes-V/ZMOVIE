package com.zz.zmovie.rabbitMq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MqService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RabbitTemplate rabbitTemplate;
    private String exchangeName = "ZMovieExchange";

    public int updateMsg (String routerKey,String msg) {
        logger.info("发送消息："+routerKey);

        rabbitTemplate.convertAndSend(exchangeName, routerKey,msg);
        return 1;
    }

//    public int updateCollected(String routerKey,String msg){
//        logger.info("发送消息："+msg);
//
//        rabbitTemplate.convertAndSend(exchangeName, routerKey,msg);
//        return 1;
//    }
}
