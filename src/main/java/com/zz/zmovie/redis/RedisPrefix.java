package com.zz.zmovie.redis;



public interface RedisPrefix {
    int getExpireSeconds();
    String getPrefix();

}
