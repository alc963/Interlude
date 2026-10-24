package com.naugroup3.interlude.utils.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JWTUtils jwt_utils;
    private final UserDetailsService user_details_service;
    private final HandlerExceptionResolver handler_exception_resolver;

    public JWTAuthenticationFilter(
        UserDetailsService user_details_service,
        @Qualifier("handlerExceptionResolver") HandlerExceptionResolver handler_exception_resolver
    ) {
        this.jwt_utils = new JWTUtils();
        this.user_details_service = user_details_service;
        this.handler_exception_resolver = handler_exception_resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter_chain) throws ServletException, IOException {
        final String auth_header = request.getHeader("Authorization");

        if (auth_header == null || !auth_header.startsWith("Bearer ")) {
            filter_chain.doFilter(request, response);
            return;
        }

        final String jwt = auth_header.substring(7);

        try {
            final String user_email = jwt_utils.extract_email(jwt);

            if (user_email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                final UserDetails user_details = this.user_details_service.loadUserByUsername(user_email);

                if (jwt_utils.validate_token(jwt, user_details)) {
                    final UsernamePasswordAuthenticationToken auth_token = new UsernamePasswordAuthenticationToken(user_details, null, user_details.getAuthorities());
                    final WebAuthenticationDetails details = new WebAuthenticationDetailsSource().buildDetails(request);
                    auth_token.setDetails(details);
                    SecurityContextHolder.getContext().setAuthentication(auth_token);
                }
            }
            filter_chain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            handler_exception_resolver.resolveException(request, response, null, e);
        }
    }
}