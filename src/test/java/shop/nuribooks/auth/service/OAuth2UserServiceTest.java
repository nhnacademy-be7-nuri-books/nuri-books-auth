package shop.nuribooks.auth.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.context.SpringBootTest;

import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.dto.OAuth2User;
import shop.nuribooks.auth.dto.MemberResponse;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

import java.io.IOException;

@SpringBootTest
public class OAuth2UserServiceTest {

    @Mock
    private MemberFeignClient memberFeignClient;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private HttpServletResponse response;

    private OAuth2UserService oAuth2UserService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        oAuth2UserService = new OAuth2UserService(memberFeignClient, refreshTokenRepository, jwtUtils);
    }

    @Test
    public void testLogin_WhenUserAlreadyRegistered() throws IOException {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        MemberResponse memberResponse = new MemberResponse("user_id", "user_password", "USER", 123L, "ACTIVE");

        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(memberResponse));

        // When
        ResponseEntity<String> result = oAuth2UserService.login(oAuth2User, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("LOGIN_SUCCESS", result.getBody());
        verify(memberFeignClient, times(1)).findByUsername(oAuth2User.id());
    }

    @Test
    public void testLogin_WhenEmailExistsButNotRegistered() {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        MemberResponse memberResponse = new MemberResponse("user_id", "user_password", "USER", 123L, "ACTIVE");

        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(memberResponse));
        when(memberFeignClient.findByEmail(oAuth2User.email())).thenReturn(ResponseEntity.ok(memberResponse));

        // When
        ResponseEntity<String> result = oAuth2UserService.login(oAuth2User, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("LOGIN_SUCCESS", result.getBody());
    }

    @Test
    public void testLogin_WhenUserNotRegistered() {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");

        // When: memberFeignClient에서 findByEmail이 null을 반환하도록 설정
        when(memberFeignClient.findByEmail(oAuth2User.email())).thenReturn(ResponseEntity.ok(null));
        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(null)); // 여기도 모킹을 추가

        // When: 로그인 시도
        ResponseEntity<String> result = oAuth2UserService.login(oAuth2User, response);

        // Then: 등록되지 않은 사용자여야 하므로 "NEED_REGISTER" 반환
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("NEED_REGISTER", result.getBody());
    }

}
