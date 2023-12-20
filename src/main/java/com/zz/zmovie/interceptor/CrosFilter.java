package com.zz.zmovie.interceptor;//package com.zz.zmovie.Interceptor;
//
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//public class CrosFilter  {
//
////    @Override
////    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
////            throws IOException, ServletException {
////        HttpServletResponse res = (HttpServletResponse) response;
////        res.addHeader("Access-Control-Allow-Credentials", "true");
////        res.addHeader("Access-Control-Allow-Origin", "*");
////        res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
////        res.addHeader("Access-Control-Allow-Headers", "Content-Type,X-CAF-Authorization-Token,sessionToken,X-TOKEN");
////        if (((HttpServletRequest) request).getMethod().equals("OPTIONS")) {
////            response.getWriter().println("ok");
////            return;
////        }
////        chain.doFilter(request, response);
////    }
//@Bean
//public FilterRegistrationBean corsFilter() {
//    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//    CorsConfiguration config = new CorsConfiguration();
//    config.setAllowCredentials(true);
//    // 使用setAllowedOrigin会出现IllegalArgumentException
//    config.addAllowedOriginPattern("*");
//    config.addAllowedHeader("*");
//    config.addAllowedMethod("*");
//    source.registerCorsConfiguration("/**", config);
//    FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
//    bean.setOrder(0);
//    return bean;
//}
//
//
////    @Override
////    public void destroy() {
////    }
////
////    @Override
////    public void init(FilterConfig filterConfig) throws ServletException {
////    }
//
//}
