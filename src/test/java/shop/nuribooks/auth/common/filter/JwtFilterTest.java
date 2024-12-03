package shop.nuribooks.auth.common.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import shop.nuribooks.auth.common.util.JwtUtils;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(jwtUtils); // JwtUtils가 제대로 Mock으로 주입되는지 확인
        mockMvc = MockMvcBuilders
                .standaloneSetup(new Object()) // 필터만 테스트
                .addFilters(jwtFilter)
                .build();
    }

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDownSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void testJwtFilterWithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isNotFound());

        verify(jwtUtils, never()).isExpired(anyString());
        verify(jwtUtils, never()).getUsername(anyString());
    }

    @Test
    void testJwtFilterWithInvalidAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "InvalidToken"))
                .andExpect(status().isNotFound());

        verify(jwtUtils, never()).isExpired(anyString());
        verify(jwtUtils, never()).getUsername(anyString());
    }

    @Test
    void testJwtFilterWithExpiredToken() throws Exception {
        String expiredToken = "expired.jwt.token";
        when(jwtUtils.isExpired(expiredToken)).thenReturn(true);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isNotFound());

        verify(jwtUtils, times(1)).isExpired(expiredToken);
        verify(jwtUtils, never()).getUsername(anyString());
    }

    @Test
    void testJwtFilterWithValidToken() throws Exception {
        String validToken = "valid.jwt.token";
        String username = "testUser";
        String role = "ROLE_USER";

        when(jwtUtils.isExpired(validToken)).thenReturn(false);
        when(jwtUtils.getUsername(validToken)).thenReturn(username);
        when(jwtUtils.getRole(validToken)).thenReturn(role);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());

        verify(jwtUtils, times(1)).isExpired(validToken);
        verify(jwtUtils, times(1)).getUsername(validToken);
        verify(jwtUtils, times(1)).getRole(validToken);
    }

    @Test
    void testJwtFilterWithValidTokenChecksSecurityContext() throws Exception {
        String validToken = "valid.jwt.token";
        String username = "testUser";
        String role = "ROLE_USER";

        when(jwtUtils.isExpired(validToken)).thenReturn(false);
        when(jwtUtils.getUsername(validToken)).thenReturn(username);
        when(jwtUtils.getRole(validToken)).thenReturn(role);

        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role)));
    }

    @Test
    void testJwtFilterWithNullUsername() throws Exception {
        // Given
        String validToken = "valid.jwt.token";

        when(jwtUtils.isExpired(validToken)).thenReturn(false);
        when(jwtUtils.getUsername(validToken)).thenReturn(null); // username을 null로 설정
        when(jwtUtils.getRole(validToken)).thenReturn("ROLE_USER");

        // When & Then
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication); // SecurityContext에 값이 없어야 함
    }

    @Test
    void testJwtFilterWithNullRole() throws Exception {
        // Given
        String validToken = "valid.jwt.token";

        when(jwtUtils.isExpired(validToken)).thenReturn(false);
        when(jwtUtils.getUsername(validToken)).thenReturn("testUser");
        when(jwtUtils.getRole(validToken)).thenReturn(null); // role을 null로 설정

        // When & Then
        mockMvc.perform(get("/api/test")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isNotFound()); // JwtFilter는 SecurityContext를 설정하지 않고 통과해야 함

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication); // SecurityContext에 값이 없어야 함
    }
}
