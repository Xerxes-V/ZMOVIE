package com.zz.zmovie.redis;

import com.zz.zmovie.po.User;

public class UserPrefix extends BasePrefix {

    private static final int userExpire = 2*24*3600; //用户的过期时间时 两天

    public static UserPrefix userToken = new UserPrefix("token",userExpire);          //token的前缀

    public static UserPrefix getUserByAccount = new UserPrefix("User");

    public static UserPrefix userCollections = new UserPrefix("collections",userExpire);    //用户收藏列表

    public static UserPrefix userHistory = new UserPrefix("history",userExpire);

    public static UserPrefix userScoreList = new UserPrefix("userScoreList",userExpire);

    public UserPrefix(String prefix,int expireSeconds){
        super(prefix,expireSeconds);
    }

    public UserPrefix(String prefix) {
        super(prefix);
    }
}
