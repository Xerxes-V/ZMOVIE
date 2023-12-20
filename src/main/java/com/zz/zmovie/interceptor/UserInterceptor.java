package com.zz.zmovie.interceptor;

import cn.hutool.core.util.StrUtil;
import com.zz.zmovie.service.UserService;
import com.zz.zmovie.config.UserThreadLocal;
import com.zz.zmovie.po.User;
import com.zz.zmovie.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用以先拦截所有请求，获取用户信息后再放行
 */

@Configuration
public class UserInterceptor implements HandlerInterceptor {
    private static Logger logger = LoggerFactory.getLogger(UserInterceptor.class);

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.info("拦截方法：{}", handler);

        String token = getCookie(response, request);

        if (!StrUtil.isEmpty(token)) {
            User user = userService.getByToken(response, token);
            UserThreadLocal.set(user);
        }
        return true;
    }

    //    public
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocal.remove();
    }

    //从 cookies 中过滤出 token
    public String getCookie(HttpServletResponse response, @org.jetbrains.annotations.NotNull HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
