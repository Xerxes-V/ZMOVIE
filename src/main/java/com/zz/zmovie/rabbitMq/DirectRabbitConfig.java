package com.zz.zmovie.rabbitMq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectRabbitConfig {

    //队列 用户
    @Bean
    public Queue userQueue() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。
        //   return new Queue("TestDirectQueue",true,true,false);

        //一般设置一下队列的持久化就好,其余两个就是默认false
        return new Queue("ZMovie_user",true);
    }


    //队列：用户评分：
    @Bean
    public Queue userScoreQueue() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。
        //   return new Queue("TestDirectQueue",true,true,false);

        //一般设置一下队列的持久化就好,其余两个就是默认false
        return new Queue("ZMovie_userScore",true);
    }

    //队列：收藏列表：
    @Bean
    public Queue collectionsQueue() {
        return new Queue("ZMovie_collections",true);
    }

    //队列：电影信息
    @Bean
    public Queue movieQueue() {
        return new Queue("ZMovie_movie",true);
    }

    //队列：电影详情
    @Bean
    public Queue movieDetailQueue() {
        return new Queue("ZMovie_movie_detail",true);
    }


    //Direct交换机 起名：ZMovieExchange
    @Bean
    DirectExchange ZMovieDirectExchange() {
        //  return new DirectExchange("TestDirectExchange",true,true);
        return new DirectExchange("ZMovieExchange",true,false);
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：ZMovie_user
    @Bean
    Binding bindingDirect() {
        return BindingBuilder.bind(userQueue()).to(ZMovieDirectExchange()).with("user");
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：ZMovie_userScore:
    @Bean
    Binding bindingDirectUserScore() {
        return BindingBuilder.bind(userScoreQueue()).to(ZMovieDirectExchange()).with("userScore");
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：ZMovie_collections
    @Bean
    Binding bindingDirectCollections() {
        return BindingBuilder.bind(collectionsQueue()).to(ZMovieDirectExchange()).with("collections");
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：ZMovie_movie
    @Bean
    Binding bindingDirectMovie() {
        return BindingBuilder.bind(movieQueue()).to(ZMovieDirectExchange()).with("movie");
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键：ZMovie_movie_detail
    @Bean
    Binding bindingDirectMovieD() {
        return BindingBuilder.bind(movieDetailQueue()).to(ZMovieDirectExchange()).with("movieDetail");
    }



    @Bean
    DirectExchange lonelyDirectExchange() {
        return new DirectExchange("lonelyDirectExchange");
    }



}