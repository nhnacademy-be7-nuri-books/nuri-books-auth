package shop.nuribooks.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;

import shop.nuribooks.auth.common.exception.BadRequestException;
import shop.nuribooks.auth.common.exception.NotFoundException;
import shop.nuribooks.auth.entity.RefreshToken;
import shop.nuribooks.auth.repository.RefreshTokenRepository;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.common.util.CookieUtils;

public class ReissueServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtils jwtUtils;

    private ReissueService reissueService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reissueService = new ReissueService(refreshTokenRepository, jwtUtils);
    }

    @Test
    void testReissue_WhenRefreshTokenIsNull() {
        // Given
        String refreshToken = null;
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When & Then
        BadRequestException exception = org.junit.jupiter.api.Assertions.assertThrows(BadRequestException.class, () -> {
            reissueService.reissue(refreshToken, response);
        });

        assertEquals("Refresh Token is NULL or Empty", exception.getMessage());
    }

    @Test
    void testReissue_WhenRefreshTokenIsExpired() {
        // Given
        String refreshToken = "expiredRefreshToken";
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.isExpired(refreshToken)).thenReturn(true);

        // When & Then
        BadRequestException exception = org.junit.jupiter.api.Assertions.assertThrows(BadRequestException.class, () -> {
            reissueService.reissue(refreshToken, response);
        });

        assertEquals("Refresh Token is Expired.", exception.getMessage());
    }

    @Test
    void testReissue_WhenRefreshTokenDoesNotExist() {
        // Given
        String refreshToken = "nonExistingRefreshToken";
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtils.isExpired(refreshToken)).thenReturn(false);
        when(refreshTokenRepository.findByRefresh(refreshToken)).thenReturn(null);

        // When & Then
        NotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(NotFoundException.class, () -> {
            reissueService.reissue(refreshToken, response);
        });

        assertEquals("Refresh Token is NOT EXISTS", exception.getMessage());
    }

    @Test
    void testReissue_WhenTokenIsValid() {
        // Given
        String refreshToken = "validRefreshToken";
        String username = "user1";
        String role = "USER";
        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        MockHttpServletResponse response = new MockHttpServletResponse();

        RefreshToken existingToken = new RefreshToken();
        existingToken.setRefresh(refreshToken);
        existingToken.setAccess("oldAccessToken");
        existingToken.setExpiration(Instant.now().toString());

        when(jwtUtils.isExpired(refreshToken)).thenReturn(false);
        when(jwtUtils.getUsername(refreshToken)).thenReturn(username);
        when(jwtUtils.getRole(refreshToken)).thenReturn(role);
        when(jwtUtils.createJwt("Access", username, role, JwtUtils.ACCESS_TOKEN_VALID_TIME)).thenReturn(newAccessToken);
        when(jwtUtils.createJwt("Refresh", username, role, JwtUtils.REFRESH_TOKEN_VALID_TIME)).thenReturn(newRefreshToken);
        when(refreshTokenRepository.findByRefresh(refreshToken)).thenReturn(existingToken);

        // When
        ResponseEntity<Void> responseEntity = reissueService.reissue(refreshToken, response);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(newAccessToken, response.getHeader("Authorization"));
        assertNotNull(response.getCookie("Refresh"));
        assertEquals(newAccessToken, existingToken.getAccess());
        assertEquals(newRefreshToken, existingToken.getRefresh());
    }
}
