package com.example.demo.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private final String SECRET = "mysecretkey12345";
    private final long EXPIRATION_MS = 1000 * 60 * 60 * 24; // 24 hrs

    public String generateToken(String customerUuid) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("customerId", customerUuid);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(customerUuid)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public String extractCustomerUuid(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
