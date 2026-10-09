package com.twiiiins.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class AdminTokenFilter extends OncePerRequestFilter {
    private final AdminSessionRepository sessions;
    public AdminTokenFilter(AdminSessionRepository sessions) { this.sessions = sessions; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            sessions.findById(Tokens.hash(header.substring(7))).filter(s -> s.getExpiresAt().isAfter(Instant.now())).ifPresent(s ->
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(s.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))));
        }
        chain.doFilter(request, response);
    }
}
