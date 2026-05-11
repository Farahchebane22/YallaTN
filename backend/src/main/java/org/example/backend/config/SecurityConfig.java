package org.example.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Value("${app.cors.allowed-origins:http://localhost:4200}")
        private String allowedOrigins;

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        OAuth2AuthenticationSuccessHandler successHandler,
                        OAuth2AuthenticationFailureHandler failureHandler) throws Exception {

                return http

                                // =========================
                                // CSRF
                                // =========================
                                .csrf(AbstractHttpConfigurer::disable)

                                // =========================
                                // CORS
                                // =========================
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // =========================
                                // SESSIONLESS JWT
                                // =========================
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // =========================
                                // 401 HANDLER
                                // =========================
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(restAuthenticationEntryPoint()))

                                // =========================
                                // AUTHORIZATION
                                // =========================
                                .authorizeHttpRequests(auth -> auth

                                                // PREFLIGHT
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                // ACTUATOR / PROMETHEUS
                                                .requestMatchers("/api/actuator/**").permitAll()

                                                // AUTH
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/auth/**",
                                                                "/oauth2/**",
                                                                "/login/**",
                                                                "/login/oauth2/**")
                                                .permitAll()

                                                // PUBLIC APIs
                                                .requestMatchers("/api/public/**").permitAll()
                                                .requestMatchers("/public/**").permitAll()

                                                // STATIC
                                                .requestMatchers(
                                                                "/uploads/**",
                                                                "/media/**")
                                                .permitAll()

                                                // WEBSOCKET
                                                .requestMatchers(
                                                                "/ws/**",
                                                                "/api/ws/**",
                                                                "/ws-native/**")
                                                .permitAll()

                                                // PUBLIC GET APIs
                                                .requestMatchers(HttpMethod.GET,

                                                                "/api/cities",
                                                                "/api/cities/**",

                                                                "/api/transports",
                                                                "/api/transports/**",

                                                                "/api/cars",
                                                                "/api/cars/**",

                                                                "/api/flights",
                                                                "/api/flights/**",

                                                                "/api/events",
                                                                "/api/events/**",

                                                                "/api/products",
                                                                "/api/products/**",

                                                                "/api/accommodations",
                                                                "/api/accommodations/**",

                                                                "/api/geo",
                                                                "/api/geo/**",

                                                                "/api/routes",
                                                                "/api/routes/**",

                                                                "/api/currency/**")
                                                .permitAll()

                                                // ADMIN
                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMIN")

                                                // SOCIAL FEATURES
                                                .requestMatchers(
                                                                "/post/**",
                                                                "/comment/**",
                                                                "/like/**",
                                                                "/story/**")
                                                .authenticated()

                                                // EVERYTHING UNDER /api REQUIRES AUTH
                                                .requestMatchers("/api/**")
                                                .authenticated()

                                                // EVERYTHING ELSE
                                                .anyRequest()
                                                .permitAll())

                                // =========================
                                // OAUTH2
                                // =========================
                                .oauth2Login(oauth2 -> oauth2
                                                .successHandler(successHandler)
                                                .failureHandler(failureHandler))

                                // =========================
                                // JWT FILTER
                                // =========================
                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class)

                                .build();
        }

        // =========================
        // 401 HANDLER
        // =========================
        @Bean
        public AuthenticationEntryPoint restAuthenticationEntryPoint() {
                return (request, response, authException) -> response.sendError(
                                HttpServletResponse.SC_UNAUTHORIZED,
                                "Unauthorized");
        }

        // =========================
        // PASSWORD ENCODER
        // =========================
        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // =========================
        // AUTH MANAGER
        // =========================
        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        // =========================
        // CORS
        // =========================
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {

                CorsConfiguration config = new CorsConfiguration();

                config.setAllowedOrigins(
                                Arrays.stream(allowedOrigins.split(","))
                                                .map(String::trim)
                                                .collect(Collectors.toList()));

                config.setAllowedMethods(List.of(
                                "GET",
                                "POST",
                                "PUT",
                                "DELETE",
                                "PATCH",
                                "OPTIONS"));

                config.setAllowedHeaders(List.of(
                                "*"));

                config.setExposedHeaders(List.of(
                                "Authorization"));

                config.setAllowCredentials(true);

                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

                source.registerCorsConfiguration("/**", config);

                return source;
        }
}