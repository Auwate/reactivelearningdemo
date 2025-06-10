package com.reactivelearning.demo.security.config;

import com.reactivelearning.demo.security.filters.CookieFilter;
import com.reactivelearning.demo.security.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class FilterConfig {

    @Bean
    @Order(1)
    public CookieFilter cookieFilter(JwtUtil jwtUtil) {
        return new CookieFilter(jwtUtil);
    }

}
