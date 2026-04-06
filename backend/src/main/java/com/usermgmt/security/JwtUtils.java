package com.usermgmt.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtils {
    @Value("${app.jwt.secret}") private String jwtSecret;
    @Value("${app.jwt.expiration}") private long jwtExpiration;
    @Value("${app.jwt.refresh-expiration}") private long refreshExpiration;

    private Key getSigningKey() { return Keys.hmacShaKeyFor(jwtSecret.getBytes()); }

    public String generateToken(UserDetails u) { return createToken(new HashMap<>(), u.getUsername(), jwtExpiration); }
    public String generateRefreshToken(UserDetails u) { return createToken(new HashMap<>(), u.getUsername(), refreshExpiration); }

    private String createToken(Map<String,Object> claims, String subject, long exp) {
        return Jwts.builder().setClaims(claims).setSubject(subject)
            .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+exp))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }
    public String extractUsername(String token) { return extractClaim(token, Claims::getSubject); }
    public <T> T extractClaim(String token, Function<Claims,T> resolver) {
        return resolver.apply(Jwts.parserBuilder().setSigningKey(getSigningKey())
            .build().parseClaimsJws(token).getBody());
    }
    public boolean validateToken(String token, UserDetails u) {
        return extractUsername(token).equals(u.getUsername()) &&
            !extractClaim(token,Claims::getExpiration).before(new Date());
    }
}