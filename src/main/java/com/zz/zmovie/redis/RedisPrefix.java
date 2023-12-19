package com.zz.zmovie.redis;



public interface RedisPrefix {
    public int getExpireSeconds();
    public String getPrefix();

}
