package com.naugroup3.interlude.utils.jwt;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JWTAuthenticationFilter jwt_auth_filter;
    private final AuthenticationProvider authentication_provider;

    @Bean
    public CorsConfigurationSource cors_configuration_source() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://127.0.0.1:8081", "https://interlude.sumii.me"));
        config.setAllowCredentials(true);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain security_filter_chain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(cors_configuration_source()))
            .csrf(csrf -> csrf.disable()) // no cross site request forgery since stateless
            .authorizeHttpRequests(auth -> auth.requestMatchers("/auth/**", "/health", "/error").permitAll().anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authentication_provider)
            .addFilterBefore(jwt_auth_filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
