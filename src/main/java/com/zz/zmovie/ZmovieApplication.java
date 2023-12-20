package com.zz.zmovie;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@MapperScan("com.zz.zmovie.mapper")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})

public class ZmovieApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZmovieApplication.class, args);
    }


    @Configuration
    public class CrosConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET","HEAD","POST","DELETE","OPTIONS","PUT")
                    .allowCredentials(true)
                    .maxAge(3600)
                    .allowedHeaders("*");
        }
    }
}
