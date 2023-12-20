package com.zz.zmovie.redis;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
public class RedisConfig {
    private String host = "47.107.31.98";
    private int port = 6379;
    private Integer timeout = 100;//秒
    private String password = "991128";
    private int poolMaxTotal = 8;
    private int poolMaxIdle = 8;
    private int poolMaxWait = 500;//秒

}
