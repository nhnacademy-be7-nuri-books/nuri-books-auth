package shop.nuribooks.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import shop.nuribooks.auth.dto.OAuth2User;
import shop.nuribooks.auth.service.OAuth2UserService;

import jakarta.servlet.http.HttpServletResponse;

class OAuth2ControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OAuth2UserService oAuth2UserService;

    @InjectMocks
    private OAuth2Controller oAuth2Controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(oAuth2Controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Given
        OAuth2User oAuth2User = new OAuth2User("oauthId123", "test@example.com");
        when(oAuth2UserService.login(any(OAuth2User.class), any(HttpServletResponse.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body("LOGIN_SUCCESS"));

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(oAuth2User)))
                .andExpect(status().isOk())
                .andExpect(content().string("LOGIN_SUCCESS"));
    }

    @Test
    void testAlreadyExists() throws Exception {
        // Given
        OAuth2User oAuth2User = new OAuth2User("oauthId456", "existing@example.com");
        when(oAuth2UserService.login(any(OAuth2User.class), any(HttpServletResponse.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body("ALREADY_EXISTS"));

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(oAuth2User)))
                .andExpect(status().isOk())
                .andExpect(content().string("ALREADY_EXISTS"));
    }

    @Test
    void testNeedRegister() throws Exception {
        // Given
        OAuth2User oAuth2User = new OAuth2User("oauthId789", "newuser@example.com");
        when(oAuth2UserService.login(any(OAuth2User.class), any(HttpServletResponse.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body("NEED_REGISTER"));

        // When & Then
        mockMvc.perform(post("/api/auth/oauth2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(oAuth2User)))
                .andExpect(status().isOk())
                .andExpect(content().string("NEED_REGISTER"));
    }
}
