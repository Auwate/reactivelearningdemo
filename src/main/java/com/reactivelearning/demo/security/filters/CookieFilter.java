package com.reactivelearning.demo.security.filters;

import com.reactivelearning.demo.entities.User;
import com.reactivelearning.demo.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
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
 * - Uses ConfigurationProperties, copying from app.security.filters.cookies
 */
@Component
@ConfigurationProperties(prefix = "app.security.filters.cookies")
public class CookieFilter implements WebFilter {

    private boolean enabled = true;
    private List<String> permittedPaths = new ArrayList<>();

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private final JwtUtil jwtUtil;

    @Autowired
    public CookieFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Filter
     * - A filter that checks an incoming request for a cookie
     * @param exchange The request information
     * @param chain The tool to trigger other filters
     * @return The next filter in line
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // If the path is permitted, automatically skip.
        if (isPermittedPath(exchange.getRequest().getPath().value()) || !enabled) {
            return chain.filter(exchange);
        }

        // If the key does not exist...
        if (!exchange.getRequest().getCookies().containsKey("reactive_authn_authz")) {
            return Mono.error(new BadCredentialsException("Missing authentication cookie."));
        }

        // Gather the cookie
        HttpCookie cookie = exchange
                .getRequest()
                .getCookies()
                .getFirst("reactive_authn_authz");

        // Validate the cookie...
        if (cookie == null || !jwtUtil.isValid(cookie.getValue())) {
            return Mono.error(new BadCredentialsException("Authentication cookie is invalid"));
        }

        // Gather the user's data from the cookie
        User user = jwtUtil.extractUserFromJwt(cookie.getValue());

        // If the data is invalid...
        if (user == null) {
            return Mono.error(new BadCredentialsException("Provided identification is invalid."));
        }

        // Generate user's context
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );

        // Set the context and pass the filter
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> {
                    context.setAuthentication(token);
                    return context;
                })
                .then(chain.filter(exchange));

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