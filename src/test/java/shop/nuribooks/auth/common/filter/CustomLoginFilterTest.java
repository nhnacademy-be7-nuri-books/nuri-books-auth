package shop.nuribooks.auth.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import shop.nuribooks.auth.common.exception.InactiveUserFoundException;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.dto.LoginRequest;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(CustomLoginFilter.class)
class CustomLoginFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private MemberFeignClient memberFeignClient;

    @InjectMocks
    private CustomLoginFilter customLoginFilter;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // MockMvc 설정
        mockMvc = MockMvcBuilders.standaloneSetup(customLoginFilter).build();
    }


    @Test
    void testLoginFailure() throws Exception {
        // Given
        String username = "testUser";
        String password = "wrongPassword";
        LoginRequest loginRequest = new LoginRequest(username, password);

        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testInactiveUser() throws Exception {
        // Given
        String username = "inactiveUser";
        String password = "testPassword";
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Simulate an inactive user
        UserDetails userDetails = User.withUsername(username).password(password).roles("USER").build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.createJwt(any(), any(), any(), any())).thenReturn("fakeToken");

        // Simulating the check for inactive user
        when(memberFeignClient.informLogin(any())).thenThrow(new InactiveUserFoundException("해당 아이디는 휴면 계정입니다."));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound());
    }
}
