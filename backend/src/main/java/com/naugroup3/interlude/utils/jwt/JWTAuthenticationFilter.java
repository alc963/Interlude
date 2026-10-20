package com.naugroup3.interlude.utils.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JWTUtils jwt_utils;
    private final UserDetailsService user_details_service;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter_chain) throws ServletException, IOException {
        final String auth_header = request.getHeader("Authorization");
        final String jwt;
        final String user_email;

        if (auth_header == null || !auth_header.startsWith("Bearer ")) {
            filter_chain.doFilter(request, response);
            return;
        }

        jwt = auth_header.substring(7);

        try {
            user_email = jwt_utils.extract_username(jwt);

            if (user_email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                final UserDetails user_details = this.user_details_service.loadUserByUsername(user_email);

                if (jwt_utils.validate_token(jwt, user_details)) {
                    final UsernamePasswordAuthenticationToken auth_token = new UsernamePasswordAuthenticationToken(user_details, null, user_details.getAuthorities());
                    final WebAuthenticationDetails details = new WebAuthenticationDetailsSource().buildDetails(request);
                    auth_token.setDetails(details);
                    SecurityContextHolder.getContext().setAuthentication(auth_token);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            // Spring returns 401 or 403
        }
        filter_chain.doFilter(request, response);
    }
}