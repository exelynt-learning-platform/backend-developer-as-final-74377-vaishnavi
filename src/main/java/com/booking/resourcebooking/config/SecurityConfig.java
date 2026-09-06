package com.booking.resourcebooking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import jakarta.servlet.http.HttpServletResponse;

import com.booking.resourcebooking.security.CustomUserDetailsService;
import com.booking.resourcebooking.security.JwtAuthenticationFilter;

@EnableMethodSecurity
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
            // JWT APIs are stateless, so CSRF protection is disabled.
            .csrf(csrf -> csrf.disable())

            // Do not create HTTP sessions.
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            // Endpoint authorization rules.
            .authorizeHttpRequests(auth -> auth

                // Login endpoint is publicly accessible.
                .requestMatchers("/auth/**").permitAll()

                // Swagger/OpenAPI endpoints.
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()

                // Everything else requires authentication.
//                .anyRequest().authenticated()
//            )
//
//            // Authentication provider used for username/password login.
//            .authenticationProvider(authenticationProvider())
//
//            // Check JWT before Spring's normal username/password filter.
//            .addFilterBefore(
//                jwtAuthenticationFilter,
//                UsernamePasswordAuthenticationFilter.class
//            );
                
                .anyRequest().authenticated()
            		)
            		.exceptionHandling(exception -> exception
            		    .authenticationEntryPoint((request, response, authException) -> {
            		        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            		        response.setContentType("application/json");
            		        response.getWriter().write(
            		            "{\"status\":401,\"message\":\"Unauthorized\"}"
            		        );
            		    })
            		    .accessDeniedHandler((request, response, accessDeniedException) -> {
            		        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            		        response.setContentType("application/json");
            		        response.getWriter().write(
            		            "{\"status\":403,\"message\":\"Access denied\"}"
            		        );
            		    })
            		)
            		.authenticationProvider(authenticationProvider())
            		.addFilterBefore(
            		    jwtAuthenticationFilter,
            		    UsernamePasswordAuthenticationFilter.class
            		);

        return http.build();
    }
}
