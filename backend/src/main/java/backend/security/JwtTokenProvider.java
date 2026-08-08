package backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key; private final long expirySeconds;
    public JwtTokenProvider(@Value("${app.jwt.secret:change-this-development-secret-key-to-at-least-32-bytes}") String secret, @Value("${app.jwt.expiration-seconds:86400}") long expirySeconds) { this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expirySeconds = expirySeconds; }
    public String generateToken(UserDetails user) { Instant now = Instant.now(); return Jwts.builder().subject(user.getUsername()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirySeconds))).signWith(key).compact(); }
    public String getUsername(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
    public boolean isValid(String token) { try { getUsername(token); return true; } catch (RuntimeException ex) { return false; } }
    public long getExpirySeconds() { return expirySeconds; }
}
