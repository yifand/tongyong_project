package com.vdc.pdi.algorithminlet.validator;

import com.vdc.pdi.device.domain.entity.EdgeBox;
import com.vdc.pdi.device.domain.repository.EdgeBoxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 盒子认证器测试
 */
@ExtendWith(MockitoExtension.class)
class BoxAuthenticatorTest {

    @Mock
    private EdgeBoxRepository edgeBoxRepository;

    @InjectMocks
    private BoxAuthenticator boxAuthenticator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(boxAuthenticator, "secretKey", "test-secret-key");
        ReflectionTestUtils.setField(boxAuthenticator, "cacheExpireMinutes", 30);
        boxAuthenticator.init();
    }

    @Test
    void authenticate_ValidToken_ReturnsValidResult() {
        // Given
        String boxId = "BOX_001";
        EdgeBox box = new EdgeBox();
        box.setId(1L);
        box.setSiteId(1L);

        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(box));

        // Generate valid token
        String validToken = generateValidToken(boxId);

        // When
        BoxAuthResult result = boxAuthenticator.authenticate(validToken, boxId);

        // Then
        assertTrue(result.isValid());
        assertEquals(1L, result.getSiteId());
        assertEquals(1L, result.getBoxRecordId());
    }

    @Test
    void authenticate_InvalidToken_ReturnsInvalidResult() {
        // Given
        String boxId = "BOX_001";
        EdgeBox box = new EdgeBox();
        box.setId(1L);
        box.setSiteId(1L);

        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(box));

        // When
        BoxAuthResult result = boxAuthenticator.authenticate("invalid-token", boxId);

        // Then
        assertFalse(result.isValid());
        assertEquals("Token无效", result.getErrorMessage());
    }

    @Test
    void authenticate_BoxNotFound_ReturnsInvalidResult() {
        // Given
        String boxId = "BOX_999";

        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.empty());
        // Note: findAllByDeletedAtIsNull is called inside findBoxByCode, but the result is handled gracefully

        // When
        BoxAuthResult result = boxAuthenticator.authenticate("some-token", boxId);

        // Then
        assertFalse(result.isValid());
        assertEquals("盒子不存在", result.getErrorMessage());
    }

    @Test
    void authenticate_NullToken_ReturnsInvalidResult() {
        // When
        BoxAuthResult result = boxAuthenticator.authenticate(null, "BOX_001");

        // Then
        assertFalse(result.isValid());
        assertEquals("Token不能为空", result.getErrorMessage());
    }

    @Test
    void authenticate_EmptyToken_ReturnsInvalidResult() {
        // When
        BoxAuthResult result = boxAuthenticator.authenticate("", "BOX_001");

        // Then
        assertFalse(result.isValid());
        assertEquals("Token不能为空", result.getErrorMessage());
    }

    @Test
    void authenticate_NullBoxId_ReturnsInvalidResult() {
        // When
        BoxAuthResult result = boxAuthenticator.authenticate("token", null);

        // Then
        assertFalse(result.isValid());
        assertEquals("盒子ID不能为空", result.getErrorMessage());
    }

    @Test
    void authenticate_CacheHit_ReturnsCachedResult() {
        // Given
        String boxId = "BOX_001";
        EdgeBox box = new EdgeBox();
        box.setId(1L);
        box.setSiteId(1L);

        when(edgeBoxRepository.findByIdAndDeletedAtIsNull(any()))
                .thenReturn(Optional.of(box));

        String validToken = generateValidToken(boxId);

        // First call - should hit repository
        BoxAuthResult result1 = boxAuthenticator.authenticate(validToken, boxId);
        assertTrue(result1.isValid());

        // Second call - should hit cache
        BoxAuthResult result2 = boxAuthenticator.authenticate(validToken, boxId);
        assertTrue(result2.isValid());

        // Repository should only be called once
        verify(edgeBoxRepository, times(1)).findByIdAndDeletedAtIsNull(any());
    }

    /**
     * Generate a valid token for testing
     */
    private String generateValidToken(String boxId) {
        // This is a simplified token generation for testing
        // The actual implementation uses HMAC-SHA256
        try {
            Class<?> hmacUtils = Class.forName("org.apache.commons.codec.digest.HmacUtils");
            Object instance = hmacUtils.getMethod("hmacSha256Hex", String.class, String.class)
                    .invoke(null, "test-secret-key", boxId + ":" + java.time.LocalDate.now().toString());
            return (String) instance;
        } catch (Exception e) {
            // Fallback for testing
            return boxId + ":" + java.time.LocalDate.now().toString() + ":" + "test-secret-key".hashCode();
        }
    }
}
