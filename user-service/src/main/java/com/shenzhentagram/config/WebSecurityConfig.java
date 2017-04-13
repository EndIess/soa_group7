package com.shenzhentagram.config;

import com.shenzhentagram.authentication.JWTAuthenticateFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by Meranote on 3/6/2017.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Disable caching
        http.headers().cacheControl();

        // Disable csrf
        http.csrf().disable();

        // Authentication Config
        http.authorizeRequests()
                // permit pre-flight requests
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // permit ANY "/"
                .antMatchers("/").permitAll()
                // permit POST "/users" for registration
                .antMatchers(HttpMethod.POST, "/users").permitAll()
                // Authenticate GET "/users/self" for get current authenticated user
                .antMatchers(HttpMethod.GET, "/users/self").authenticated()
                // permit GET "/users/search" and "/users/{user_id}" for finding user
                .antMatchers(HttpMethod.GET, "/users/**").permitAll()
                // permit POST, PUT "/users/{id}/posts/count" for increment/decrement post count
                .antMatchers(HttpMethod.POST, "/users/{id}/posts/count").permitAll()
                .antMatchers(HttpMethod.PUT, "/users/{id}/posts/count").permitAll()
                // Otherwise, need authenticate
                .anyRequest().permitAll();

        // Filter Config
        // Register JWTAuthenticateFilter for filter any request to check authenticate
        JWTAuthenticateFilter.registerFilter(http);
    }

}
