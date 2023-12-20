package com.zz.zmovie.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private UserInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        //添加对用户未登录的拦截器，并添加排除项
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**")//拦截所有
                .excludePathPatterns("/js/**","/dist/images/**")//排除样式、脚本、图片等资源文件
//                .excludePathPatterns("/login/doLogin")//排除登录
                .excludePathPatterns("/login/register")//排除注册
                .excludePathPatterns("/","/index");

    }

}
