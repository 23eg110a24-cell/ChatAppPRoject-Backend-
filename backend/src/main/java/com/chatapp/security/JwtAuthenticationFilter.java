package com.chatapp.security;

import com.chatapp.domain.User;
import com.chatapp.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the bearer token from the {@code Authorization} header and, when it is valid, puts the
 * matching {@link User} entity into the security context. Requests without a usable token are
 * simply left unauthenticated and rejected later by the authorization rules.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ROLE = "ROLE_USER";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            BearerTokens.extract(request.getHeader(HttpHeaders.AUTHORIZATION))
                    .flatMap(jwtService::extractUserId)
                    .flatMap(userRepository::findById)
                    .ifPresent(user -> authenticate(user, request));
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(User user, HttpServletRequest request) {
        var authorities = List.of(new SimpleGrantedAuthority(USER_ROLE));
        var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
