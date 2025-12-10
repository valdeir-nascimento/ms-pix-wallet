package br.com.pix.wallet.infrastructure.config.security;

import br.com.pix.wallet.domain.user.UserAccount;
import br.com.pix.wallet.domain.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final String ROLES_CLAIM = "roles";

    private final PixWalletSecurityProperties securityProperties;

    public JwtTokenProvider(final PixWalletSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public TokenData generateToken(final UserAccount account) {
        final var expiration = Instant.now().plusSeconds(securityProperties.getJwt().getExpirationMinutes() * 60);
        final var token = Jwts.builder()
            .subject(account.getUsername())
            .claim(ROLES_CLAIM, account.getRoles().stream().map(Enum::name).toList())
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(expiration))
            .signWith(secretKey())
            .compact();

        return new TokenData(token, expiration);
    }

    public boolean isValid(final String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extractUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    public Set<UserRole> extractRoles(final String token) {
        final var claims = parseClaims(token);
        final List<?> rawRoles = claims.get(ROLES_CLAIM, List.class);

        if (rawRoles == null) {
            return Set.of();
        }

        return rawRoles.stream()
            .map(Object::toString)
            .map(UserRole::from)
            .collect(Collectors.toSet());
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
            .verifyWith(secretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey secretKey() {
        final var secret = securityProperties.getJwt().getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured");
        }
        final var keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 bytes. Configure pix.wallet.security.jwt.secret or JWT_SECRET env var.");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public record TokenData(String token, Instant expiresAt) {
    }
}

