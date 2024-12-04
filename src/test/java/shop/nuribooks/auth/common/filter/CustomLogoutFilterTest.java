package shop.nuribooks.auth.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.repository.RefreshTokenRepository;
import shop.nuribooks.auth.common.message.ErrorResponse;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomLogoutFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private CustomLogoutFilter customLogoutFilter;

    @BeforeEach
    void setUp() {
        // MockMvc 초기화
        mockMvc = MockMvcBuilders.standaloneSetup(customLogoutFilter)
                .addFilters(customLogoutFilter) // 필터 추가
                .build();
    }

    @Test
    void testLogout_ValidRefreshToken() throws Exception {
        // Given
        String refreshToken = "valid-refresh-token";

        when(jwtUtils.getTokenType(refreshToken)).thenReturn("Refresh");
        when(jwtUtils.isExpired(refreshToken)).thenReturn(false);
        when(refreshTokenRepository.existsByRefresh(refreshToken)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Refresh", refreshToken))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(cookie().exists("Refresh")); // Refresh 쿠키 삭제 확인
    }

    @Test
    void testLogout_TokenIsNull() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().json(new ObjectMapper().writeValueAsString(
                        new ErrorResponse(400, "Refresh Token is NULL or Empty.", "/api/auth/logout")
                )));
    }

    @Test
    void testLogout_InvalidTokenType() throws Exception {
        // Given
        String refreshToken = "invalid-token";

        when(jwtUtils.getTokenType(refreshToken)).thenReturn("Access");

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Refresh", refreshToken))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().json(new ObjectMapper().writeValueAsString(
                        new ErrorResponse(400, "Refresh Token is Invalid.", "/api/auth/logout")
                )));
    }

    @Test
    void testLogout_ExpiredToken() throws Exception {
        // Given
        String refreshToken = "expired-token";

        when(jwtUtils.getTokenType(refreshToken)).thenReturn("Refresh");
        when(jwtUtils.isExpired(refreshToken)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Refresh", refreshToken))
                .andExpect(status().isBadRequest()) // 400 BAD REQUEST
                .andExpect(content().json(new ObjectMapper().writeValueAsString(
                        new ErrorResponse(400, "Refresh Token is Expired.", "/api/auth/logout")
                )));
    }

    @Test
    void testLogout_TokenDoesNotExist() throws Exception {
        // Given
        String refreshToken = "nonexistent-token";

        when(jwtUtils.getTokenType(refreshToken)).thenReturn("Refresh");
        when(refreshTokenRepository.existsByRefresh(refreshToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Refresh", refreshToken))
                .andExpect(status().isNotFound()) // 404 NOT FOUND
                .andExpect(content().json(new ObjectMapper().writeValueAsString(
                        new ErrorResponse(404, "Refresh Token does not Exist.", "/api/auth/logout")
                )));
    }
}
