package com.reactivelearning.demo.security.config;

import com.reactivelearning.demo.security.filters.CookieFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class FilterConfig {

    @Bean
    @Order(1)
    public CookieFilter cookieFilter() {
        return new CookieFilter();
    }

}
