package shop.nuribooks.auth.common.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import shop.nuribooks.auth.common.exception.UnauthorizedException;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = mock(JwtProperties.class);

        // A secure key with at least 32 bytes
        when(jwtProperties.getSecretKey()).thenReturn("aVeryStrongSecretKeyForJwtTesting123!");
        when(jwtProperties.getIssuer()).thenReturn("nuribooks.shop");

        jwtUtils = new JwtUtils(jwtProperties);
    }

    @Test
    void testCreateJwtAndExtractClaims() {
        // Given
        String tokenType = "Access";
        String userId = "user123";
        String role = "ROLE_USER";
        Long expiredMs = JwtUtils.ACCESS_TOKEN_VALID_TIME;

        // When
        String token = jwtUtils.createJwt(tokenType, userId, role, expiredMs);

        // Then
        assertNotNull(token);

        String extractedTokenType = jwtUtils.getTokenType(token);
        String extractedUserId = jwtUtils.getUsername(token);
        String extractedRole = jwtUtils.getRole(token);

        assertEquals(tokenType, extractedTokenType);
        assertEquals(userId, extractedUserId);
        assertEquals(role, extractedRole);
    }

    @Test
    void testIsExpired_False() {
        // Given
        String token = jwtUtils.createJwt("Access", "user123", "ROLE_USER", JwtUtils.ACCESS_TOKEN_VALID_TIME);

        // When
        boolean isExpired = jwtUtils.isExpired(token);

        // Then
        assertFalse(isExpired, "Token should not be expired");
    }

    @Test
    void testIsExpired_True() {
        // Given
        String token = jwtUtils.createJwt("Access", "user123", "ROLE_USER", -1000L); // Already expired

        // When
        boolean isExpired = jwtUtils.isExpired(token);

        // Then
        assertTrue(isExpired, "Token should be expired");
    }

    @Test
    void testGetTokenType_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // Then
        assertThrows(UnauthorizedException.class, () -> jwtUtils.getTokenType(invalidToken));
    }

    @Test
    void testGetUsername_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // Then
        assertThrows(UnauthorizedException.class, () -> jwtUtils.getUsername(invalidToken));
    }

    @Test
    void testGetRole_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // Then
        assertThrows(UnauthorizedException.class, () -> jwtUtils.getRole(invalidToken));
    }
}
