package com.example.demo.security;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/v1/customer/login") || path.equals("/api/v1/customer/signup");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // 1. Read cookies from request
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 2. Token must exist
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }


        try {
            // 3. Validate token
            jwtService.validateToken(token);

            // 4. Extract data
            String customerUuid = jwtService.extractCustomerUuid(token);
            String role = jwtService.extractRole(token);

            System.out.println(role);

            // 5. Convert role to authority
            List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role));






            // 6. Create Authentication
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            customerUuid,  // principal
                            null,
                            authorities
                    );



            // 7. Store in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
            return;
        }

        // 8. Continue filter chain
        filterChain.doFilter(request, response);
    }
//something


}

