package com.naugroup3.interlude.utils.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JWTAuthenticationFilter jwt_auth_filter;
    private final AuthenticationProvider authentication_provider;

    @Bean
    public SecurityFilterChain security_filter_chain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // no cross site request forgery since stateless
            .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**").permitAll().anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authentication_provider)
            .addFilterBefore(jwt_auth_filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
