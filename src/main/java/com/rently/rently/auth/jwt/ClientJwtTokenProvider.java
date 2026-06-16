package com.rently.rently.auth.jwt;

import com.rently.rently.reservation.client.ClientAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class ClientJwtTokenProvider {

    private static final String TYPE_CLAIM = "type";
    private static final String TYPE_VALUE = "CLIENT";
    private static final String AGENCY_SLUG_CLAIM = "agencySlug";

    private final SecretKey key;
    private final long expiration;

    public ClientJwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    public String generateToken(ClientAccount client, String agencySlug) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(client.getId())
                .claim(TYPE_CLAIM, TYPE_VALUE)
                .claim(AGENCY_SLUG_CLAIM, agencySlug)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return TYPE_VALUE.equals(claims.get(TYPE_CLAIM));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractClientId(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractAgencySlug(String token) {
        return extractClaims(token).get(AGENCY_SLUG_CLAIM, String.class);
    }
}
