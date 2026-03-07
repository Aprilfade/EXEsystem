package com.ice.exebackend.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil 工具类测试
 * 测试 JWT Token 的生成、验证、解析等核心功能
 */
@DisplayName("JWT工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = Base64.getEncoder().encodeToString(
            "mySecretKeyForTestingJwtTokenGenerationAndValidation12345".getBytes()
    );
    private final Long testExpiration = 3600000L; // 1小时

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // 使用反射设置私有字段
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
        // 调用初始化方法
        jwtUtil.init();
    }

    @Test
    @DisplayName("应该成功生成Token")
    void shouldGenerateToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.singleton(new SimpleGrantedAuthority("sys:user:list")))
                .build();

        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT 应该有3个部分
    }

    @Test
    @DisplayName("应该从Token中正确提取用户名")
    void shouldExtractUsernameFromToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        String token = jwtUtil.generateToken(userDetails);

        // When
        String username = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals("admin", username);
    }

    @Test
    @DisplayName("应该从Token中正确提取过期时间")
    void shouldExtractExpirationDateFromToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("user")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        String token = jwtUtil.generateToken(userDetails);

        // When
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date())); // 应该在未来
    }

    @Test
    @DisplayName("应该验证有效Token返回true")
    void shouldValidateValidToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        String token = jwtUtil.generateToken(userDetails);

        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("应该拒绝空Token")
    void shouldRejectNullToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When & Then
        assertFalse(jwtUtil.validateToken(null, userDetails));
        assertFalse(jwtUtil.validateToken("", userDetails));
        assertFalse(jwtUtil.validateToken("   ", userDetails));
    }

    @Test
    @DisplayName("应该拒绝空UserDetails")
    void shouldRejectNullUserDetails() {
        // Given
        String token = "some.valid.token";

        // When & Then
        assertFalse(jwtUtil.validateToken(token, null));
    }

    @Test
    @DisplayName("应该拒绝用户名不匹配的Token")
    void shouldRejectTokenWithDifferentUsername() {
        // Given
        UserDetails userDetails1 = User.builder()
                .username("user1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        UserDetails userDetails2 = User.builder()
                .username("user2")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        String token = jwtUtil.generateToken(userDetails1);

        // When
        Boolean isValid = jwtUtil.validateToken(token, userDetails2);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("应该拒绝格式错误的Token")
    void shouldRejectMalformedToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // When
        Boolean isValid = jwtUtil.validateToken(malformedToken, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("应该成功为学生生成Token")
    void shouldGenerateTokenForStudent() {
        // Given
        String studentNo = "2021001";

        // When
        String token = jwtUtil.generateTokenForStudent(studentNo);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 验证学号
        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals(studentNo, username);
    }

    @Test
    @DisplayName("应该正确解析Token中的Claims")
    void shouldParseClaimsFromToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
        String token = jwtUtil.generateToken(userDetails);

        // When
        String subject = jwtUtil.getClaimFromToken(token, Claims::getSubject);
        Date issuedAt = jwtUtil.getClaimFromToken(token, Claims::getIssuedAt);

        // Then
        assertEquals("admin", subject);
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }

    @Test
    @DisplayName("应该拒绝过期的Token")
    void shouldRejectExpiredToken() throws InterruptedException {
        // Given - 创建一个很短过期时间的JwtUtil
        JwtUtil shortExpirationJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(shortExpirationJwtUtil, "expiration", 100L); // 100ms
        shortExpirationJwtUtil.init();

        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        String token = shortExpirationJwtUtil.generateToken(userDetails);

        // When - 等待Token过期
        Thread.sleep(200);

        // Then
        Boolean isValid = shortExpirationJwtUtil.validateToken(token, userDetails);
        assertFalse(isValid);
    }

    @Test
    @DisplayName("应该正确提取Token中的权限信息")
    void shouldExtractAuthoritiesFromToken() {
        // Given
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("password")
                .authorities(Collections.singleton(new SimpleGrantedAuthority("sys:user:list")))
                .build();
        String token = jwtUtil.generateToken(userDetails);

        // When
        Object authorities = jwtUtil.getClaimFromToken(token, claims -> claims.get("authorities"));

        // Then
        assertNotNull(authorities);
    }

    @Test
    @DisplayName("应该为不同用户生成不同的Token")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        UserDetails user1 = User.builder()
                .username("user1")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
        UserDetails user2 = User.builder()
                .username("user2")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("应该在不同时间生成不同的Token")
    void shouldGenerateDifferentTokensAtDifferentTimes() throws InterruptedException {
        // Given
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        String token1 = jwtUtil.generateToken(userDetails);
        Thread.sleep(10); // 等待一点时间
        String token2 = jwtUtil.generateToken(userDetails);

        // Then
        assertNotEquals(token1, token2); // 因为签发时间不同
    }
}
