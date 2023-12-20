package com.zz.zmovie.service;

import com.zz.zmovie.po.User;
import com.zz.zmovie.vo.UserData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService {
     User getByToken(HttpServletResponse response, String token) ;

    int doLogin(HttpServletResponse response, String account, String password);

    boolean register(HttpServletResponse response, String account, String password);

     User getUserByAccount(String account);

     int updatePwd(String pwd);

     boolean logout(HttpServletResponse response, HttpServletRequest request);

     UserData  getUserData();

     int updateUserName(String name, HttpServletRequest request, HttpServletResponse response);

     int updateAvatar(String path, HttpServletRequest request, HttpServletResponse response);

     int setTypes(String types, HttpServletRequest request, HttpServletResponse response);

}
