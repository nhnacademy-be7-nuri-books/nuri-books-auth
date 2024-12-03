package shop.nuribooks.auth.controller;

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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import shop.nuribooks.auth.dto.NonMemberRequest;
import shop.nuribooks.auth.dto.NonMemberResponse;
import shop.nuribooks.auth.service.NonMemberService;

@ExtendWith(MockitoExtension.class)
class NonMemberControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private NonMemberController nonMemberController;

    @Mock
    private NonMemberService nonMemberService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(nonMemberController).build();
        objectMapper = new ObjectMapper(); // ObjectMapper 수동 초기화
    }

    @Test
    void checkNonMember_validRequest_returnsOk() throws Exception {
        // Given
        NonMemberRequest nonMemberRequest = new NonMemberRequest("test@example.com", "validPassword");
        NonMemberResponse nonMemberResponse = new NonMemberResponse(1L, "test@example.com", "userName");

        // Mocking service call
        when(nonMemberService.checkNonMember(nonMemberRequest)).thenReturn(nonMemberResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/non-member/check")
                        .content(objectMapper.writeValueAsString(nonMemberRequest))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").value("userName"));

        verify(nonMemberService, times(1)).checkNonMember(nonMemberRequest);
    }

    @Test
    void checkNonMember_invalidEmail_returnsNotFound() throws Exception {
        // Given
        NonMemberRequest nonMemberRequest = new NonMemberRequest("invalid@example.com", "wrongPassword");

        // Mocking service call to return null, indicating no matching non-member
        when(nonMemberService.checkNonMember(nonMemberRequest)).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/auth/non-member/check")
                        .content(objectMapper.writeValueAsString(nonMemberRequest))
                        .contentType("application/json"))
                .andExpect(status().isOk());

        verify(nonMemberService, times(1)).checkNonMember(nonMemberRequest);
    }
}
