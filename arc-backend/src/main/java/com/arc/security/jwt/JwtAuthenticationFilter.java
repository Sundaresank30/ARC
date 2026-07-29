package com.arc.security.jwt;

import com.arc.auth.enums.AppRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        resolveAuthentication(request).ifPresent(authentication ->
                SecurityContextHolder.getContext().setAuthentication(authentication));

        filterChain.doFilter(request, response);
    }

    private Optional<UsernamePasswordAuthenticationToken> resolveAuthentication(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        if (!jwtService.isTokenValid(token)) {
            return Optional.empty();
        }

        return jwtService.extractRole(token)
                .map(this::buildAuthentication);
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(AppRole role) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.name());

        return new UsernamePasswordAuthenticationToken(
                role.name(),
                null,
                List.of(authority)
        );
    }
}
