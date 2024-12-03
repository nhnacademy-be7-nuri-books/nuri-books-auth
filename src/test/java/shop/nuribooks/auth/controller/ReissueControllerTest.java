package shop.nuribooks.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import jakarta.servlet.http.HttpServletResponse;
import shop.nuribooks.auth.common.advice.GlobalExceptionHandler;
import shop.nuribooks.auth.common.exception.NotFoundException;
import shop.nuribooks.auth.service.ReissueService;

@ExtendWith(MockitoExtension.class)
class ReissueControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReissueService reissueService;

    @InjectMocks
    private ReissueController reissueController;

    @Test
    void reissue_validRefreshToken_returnsOk() throws Exception {
        // Given
        String validRefreshToken = "valid-refresh-token";
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(reissueService.reissue(eq(validRefreshToken), any(HttpServletResponse.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc = MockMvcBuilders.standaloneSetup(reissueController).build();

        // When & Then
        mockMvc.perform(post("/api/auth/reissue")
                        .content(validRefreshToken)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        verify(reissueService).reissue(eq(validRefreshToken), any(HttpServletResponse.class));
    }

    @Test
    void reissue_emptyRefreshToken_returnsBadRequest() throws Exception {
        // Given
        String emptyRefreshToken = ""; // 요청 본문에 빈 문자열 전달
        mockMvc = MockMvcBuilders.standaloneSetup(reissueController).build();

        // When & Then
        mockMvc.perform(post("/api/auth/reissue")
                        .content(emptyRefreshToken)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest()); // 컨트롤러의 상태 코드만 확인
    }

    @Test
    void reissue_invalidRefreshToken_returnsNotFound() throws Exception {
        // Given
        String invalidRefreshToken = "invalid-refresh-token";
        doThrow(new NotFoundException("Refresh Token is NOT EXISTS"))
                .when(reissueService).reissue(eq(invalidRefreshToken), any(HttpServletResponse.class));

        mockMvc = MockMvcBuilders.standaloneSetup(reissueController)
                .setControllerAdvice(new GlobalExceptionHandler()) // 예외 핸들러 추가
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/reissue")
                        .content(invalidRefreshToken)
                        .contentType("application/json"))
                .andExpect(status().isNotFound());

        verify(reissueService).reissue(eq(invalidRefreshToken), any(HttpServletResponse.class));
    }

}
