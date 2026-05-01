package com.vdc.platform.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdc.platform.common.ApiResult;
import com.vdc.platform.dto.LoginRequest;
import com.vdc.platform.dto.LoginResponse;
import com.vdc.platform.dto.RefreshTokenRequest;
import com.vdc.platform.security.jwt.JwtUtil;
import com.vdc.platform.security.jwt.TokenService;
import com.vdc.platform.security.model.SecurityUser;
import com.vdc.platform.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.vdc.platform.VdcPlatformApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void login_withCorrectCredentials_returns200AndTokens() throws Exception {
        SecurityUser user = createUser("alice");
        UsernamePasswordAuthenticationToken authResult =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authResult);
        when(customUserDetailsService.loadUserByUsername("alice")).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("correct");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResult<LoginResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<>() {});

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getAccessToken()).isNotBlank();
        assertThat(response.getData().getRefreshToken()).isNotBlank();
        assertThat(response.getData().getUsername()).isEqualTo("alice");

        verify(customUserDetailsService).clearLoginFailure("alice");
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("wrong");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResult<?> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<>() {});

        assertThat(response.getCode()).isEqualTo(401);
        assertThat(response.getMessage()).contains("Invalid username or password");

        verify(customUserDetailsService).recordLoginFailure("alice");
    }

    @Test
    void fiveFailedLogins_lockAccountFor5Minutes() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));
        org.springframework.data.redis.core.ValueOperations<String, String> ops = mock(org.springframework.data.redis.core.ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(ops);
        when(ops.increment(any())).thenReturn(1L, 2L, 3L, 4L, 5L);

        LoginRequest request = new LoginRequest();
        request.setUsername("bob");
        request.setPassword("wrong");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        verify(customUserDetailsService, times(5)).recordLoginFailure("bob");
    }

    @Test
    void refresh_withValidRefreshToken_returnsNewAccessToken() throws Exception {
        SecurityUser user = createUser("alice");
        String refreshToken = jwtUtil.generateRefreshToken("alice");

        when(customUserDetailsService.loadUserByUsername("alice")).thenReturn(user);
        when(tokenService.validateRefreshToken(eq("alice"), any())).thenReturn(true);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        ApiResult<LoginResponse> response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new com.fasterxml.jackson.core.type.TypeReference<>() {});

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getAccessToken()).isNotBlank();
    }

    @Test
    void accessProtectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/work-sessions"))
                .andExpect(status().isUnauthorized());
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
