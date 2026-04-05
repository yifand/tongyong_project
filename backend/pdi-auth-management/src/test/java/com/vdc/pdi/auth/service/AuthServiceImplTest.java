package com.vdc.pdi.auth.service;

import com.vdc.pdi.auth.domain.entity.User;
import com.vdc.pdi.auth.domain.repository.UserRepository;
import com.vdc.pdi.auth.dto.request.LoginRequest;
import com.vdc.pdi.auth.dto.response.LoginResponse;
import com.vdc.pdi.auth.security.JwtTokenProvider;
import com.vdc.pdi.auth.security.TokenBlacklistService;
import com.vdc.pdi.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private LoginLockService loginLockService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private LoginRequest loginRequest;
    private static final String TEST_IP = "127.0.0.1";

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPassword");
        mockUser.setStatus(1);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void login_Success() {
        // Given
        when(loginLockService.isLocked("testuser")).thenReturn(false);

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("refreshToken");
        when(jwtTokenProvider.getTokenRemainingTime(anyString())).thenReturn(7200000L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        // When
        LoginResponse response = authService.login(loginRequest, TEST_IP);

        // Then
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());

        verify(loginLockService).unlock("testuser");
        verify(userRepository).updateLastLoginInfo(eq(1L), any(LocalDateTime.class), eq(TEST_IP));
    }

    @Test
    void login_AccountLocked() {
        // Given
        when(loginLockService.isLocked("testuser")).thenReturn(true);
        when(loginLockService.getRemainingLockTime("testuser")).thenReturn(5L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.login(loginRequest, TEST_IP)
        );

        assertTrue(exception.getMessage().contains("locked"));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_InvalidCredentials() {
        // Given
        when(loginLockService.isLocked("testuser")).thenReturn(false);
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.login(loginRequest, TEST_IP)
        );

        assertEquals("Invalid username or password", exception.getMessage());
        verify(loginLockService).recordFailedAttempt("testuser");
    }

    @Test
    void logout_Success() {
        // Given
        String token = "testToken";
        when(jwtTokenProvider.getTokenRemainingTime(token)).thenReturn(3600000L);

        // When
        authService.logout(token);

        // Then
        verify(tokenBlacklistService).blacklistToken(token, 3600000L);
    }

    @Test
    void refreshToken_Success() {
        // Given
        String refreshToken = "validRefreshToken";
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(refreshToken)).thenReturn(false);
        when(jwtTokenProvider.extractUsername(refreshToken)).thenReturn("testuser");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtTokenProvider.generateToken(eq(userDetails), any())).thenReturn("newAccessToken");
        when(jwtTokenProvider.generateRefreshToken(userDetails)).thenReturn("newRefreshToken");
        when(jwtTokenProvider.getTokenRemainingTime(anyString())).thenReturn(7200000L);

        // When
        LoginResponse response = authService.refreshToken(refreshToken);

        // Then
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("newRefreshToken", response.getRefreshToken());
        verify(tokenBlacklistService).blacklistToken(eq(refreshToken), anyLong());
    }

    @Test
    void refreshToken_InvalidToken() {
        // Given
        String invalidToken = "invalidToken";
        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.refreshToken(invalidToken)
        );

        assertEquals("Invalid refresh token", exception.getMessage());
    }

    @Test
    void refreshToken_BlacklistedToken() {
        // Given
        String blacklistedToken = "blacklistedToken";
        when(jwtTokenProvider.validateToken(blacklistedToken)).thenReturn(true);
        when(tokenBlacklistService.isBlacklisted(blacklistedToken)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            authService.refreshToken(blacklistedToken)
        );

        assertEquals("Refresh token has been revoked", exception.getMessage());
    }
}
