package org.example.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.backend.service.CustomUserDetailsService;
import org.example.backend.service.JwtService;
import org.example.backend.service.UserSessionService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserSessionService userSessionService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            UserSessionService userSessionService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userSessionService = userSessionService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return

        // ACTUATOR
        path.startsWith("/api/actuator")

                // AUTH
                || path.startsWith("/api/auth")
                || path.startsWith("/auth")
                || path.startsWith("/oauth2")
                || path.startsWith("/login")

                // PUBLIC APIs
                || path.startsWith("/api/public")
                || path.startsWith("/public")

                // WEBSOCKET
                || path.startsWith("/ws")
                || path.startsWith("/api/ws")

                // STATIC
                || path.startsWith("/uploads");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // NO TOKEN
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        String username;

        try {
            username = jwtService.extractUsername(token);

        } catch (Exception ex) {

            filterChain.doFilter(request, response);
            return;
        }

        // ALREADY AUTHENTICATED
        if (username != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {

                    String sessionId = jwtService.extractSessionId(token);

                    // SESSION CHECK
                    if (sessionId != null && !sessionId.isBlank()) {

                        boolean active = userSessionService.isSessionActive(
                                sessionId,
                                userDetails.getUsername());

                        if (!active) {

                            SecurityContextHolder.clearContext();

                            filterChain.doFilter(request, response);
                            return;
                        }

                        userSessionService.touchSession(sessionId);
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (Exception ex) {

                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}