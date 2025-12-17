package com.example.demo.security;

import com.example.demo.entity.CustomerRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String SECRET =
            "jnjgarnjnJNJJNJNPRJGPNENJGNEJNGEVIJGUWRGNNRjnjgarnjnJNJJNJNPRJGPNENJGNEJNGEVIJGUWRGNNR";

    private static final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 hours

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ================== GENERATE TOKEN ==================
    public String generateToken(String customerUuid, CustomerRole role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(customerUuid)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
    }

    // ================== VALIDATE TOKEN ==================
    public void validateToken(String token) {
        Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token); // throws exception if invalid
    }

    // ================== EXTRACT CUSTOMER UUID ==================
    public String extractCustomerUuid(String token) {
        return getClaims(token).getSubject();
    }

    // ================== EXTRACT ROLE ==================
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // ================== INTERNAL ==================
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
