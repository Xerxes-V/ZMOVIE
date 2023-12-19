package com.zz.zmovie.redis;

public abstract class BasePrefix implements RedisPrefix{
    int expireSeconds;
    String prefix;

    public BasePrefix(String prefix){
        this.expireSeconds = 0;
        this.prefix = prefix;
    }

    public BasePrefix(String prefix,int expireSeconds){
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int getExpireSeconds() {
        return this.expireSeconds;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }
}
