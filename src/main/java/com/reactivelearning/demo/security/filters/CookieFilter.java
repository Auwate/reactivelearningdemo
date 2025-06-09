package com.reactivelearning.demo.security.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * CookieFilter
 * - WebFlux automatically registers **every** WebFilter, thus we need to build our Filters
 * in ways that they automatically know when to apply or not.
 */
@Component
@ConfigurationProperties(prefix = "app.security.filters.cookies")
public class CookieFilter implements WebFilter {

    private boolean enabled = true;
    private List<String> permittedPaths = new ArrayList<>();

    private final PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Filter
     * - A filter that checks an incoming request for a cookie
     * @param exchange The request information
     * @param chain The tool to trigger other filters
     * @return The next filter in line
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // If the path is permitted, automatically skip.
        if (isPermittedPath(request.getPath().value())) {
            return chain.filter(exchange);
        }

        if (!request.getCookies().containsKey("reactive_authn_authz")) {
            return Mono.error(new BadCredentialsException("Missing authentication cookie."));
        }

        HttpCookie cookie = request.getCookies().getFirst("reactive_authn_authz");

        return chain.filter(exchange);

    }

    private boolean isPermittedPath(String path) {
        return this.permittedPaths
                .stream()
                .anyMatch(permitted -> pathMatcher.match(permitted, path));
    }

    // Getters / Setters

    public boolean isEnabled() {return enabled;}
    public void setEnabled(boolean enabled) {this.enabled = enabled;}

    public List<String> getPermittedPaths() {return this.permittedPaths;}
    public void setPermittedPaths(List<String> permittedPaths) {this.permittedPaths = permittedPaths;}

}