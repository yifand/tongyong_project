package com.vdc.platform.auth;

import com.vdc.platform.security.jwt.JwtUtil;
import com.vdc.platform.security.model.SecurityUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        String secret = "test-secret-key-at-least-32-characters-long!";
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // Initialize via reflection to avoid Spring context
        try {
            java.lang.reflect.Field field = JwtUtil.class.getDeclaredField("secretKey");
            field.setAccessible(true);
            field.set(jwtUtil, secretKey);

            java.lang.reflect.Field expField = JwtUtil.class.getDeclaredField("accessTokenExpiration");
            expField.setAccessible(true);
            expField.setLong(jwtUtil, 3600000L);

            java.lang.reflect.Field refField = JwtUtil.class.getDeclaredField("refreshTokenExpiration");
            refField.setAccessible(true);
            refField.setLong(jwtUtil, 86400000L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateAccessToken_and_validateToken_success() {
        SecurityUser user = createUser("alice");
        String token = jwtUtil.generateAccessToken(user);
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void getUsernameFromToken_extractsCorrectUsername() {
        SecurityUser user = createUser("bob");
        String token = jwtUtil.generateAccessToken(user);
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("bob");
    }

    @Test
    void expiredToken_isRejected() {
        String expiredToken = Jwts.builder()
                .subject("alice")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();

        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
    }

    @Test
    void malformedToken_isRejected() {
        assertThat(jwtUtil.validateToken("not.a.token")).isFalse();
    }

    private SecurityUser createUser(String username) {
        SecurityUser user = new SecurityUser();
        user.setUserId(1L);
        user.setUsername(username);
        user.setPassword("pass");
        user.setSiteId(1L);
        user.setRoleCode("ADMIN");
        user.setPermissions(List.of("read"));
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        return user;
    }
}
