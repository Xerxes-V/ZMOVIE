package com.zz.zmovie.rabbitMq;


import com.zz.zmovie.mapper.UserMapper;
import com.zz.zmovie.po.User;
import com.zz.zmovie.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "ZMovie_user")//监听的队列名称 TestDirectQueue
public class UserRabbitMqReceiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserMapper userMapper;

    @RabbitHandler
    public void receiveMessage(String message){
        logger.info("用户修改请求："+message);

        User user = RedisService.stringToBean(message,User.class);
        userMapper.updateById(user);
    }

}
