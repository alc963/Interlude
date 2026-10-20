package com.naugroup3.interlude.utils.jwt;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.naugroup3.interlude.repositories.UserRepository;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public AuthenticationManager authentication_manager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authentication_provider(UserDetailsService user_details_service, PasswordEncoder password_encoder) {
        final DaoAuthenticationProvider provider = new DaoAuthenticationProvider(user_details_service);
        provider.setPasswordEncoder(password_encoder);
        return provider;
    }

    @Bean
    public PasswordEncoder password_encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService user_details_service(UserRepository user_repository) {
        return email -> user_repository.findByEmail(email)
            .map(user -> org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword_hash())
                .authorities(Collections.emptyList())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
