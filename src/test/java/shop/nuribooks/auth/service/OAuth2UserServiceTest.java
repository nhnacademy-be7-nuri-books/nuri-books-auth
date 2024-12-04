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

import shop.nuribooks.auth.common.exception.LoginFailedException;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.dto.OAuth2User;
import shop.nuribooks.auth.dto.MemberResponse;
import shop.nuribooks.auth.entity.RefreshToken;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

import java.io.IOException;

@SpringBootTest
class OAuth2UserServiceTest {

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
    void setUp() {
        MockitoAnnotations.openMocks(this);
        oAuth2UserService = new OAuth2UserService(memberFeignClient, refreshTokenRepository, jwtUtils);
    }

    @Test
    void testLogin_WhenUserAlreadyRegistered() throws IOException {
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
    void testLogin_WhenEmailExistsButNotRegistered() {
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
    void testLogin_WhenUserNotRegistered() {
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

    @Test
    void testLogin_WhenEmailAlreadyExistsButNotOAuth2Registered() {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        MemberResponse emailExistsResponse = new MemberResponse("existing_id", "password", "USER", 456L, "ACTIVE");

        // 회원 조회 시 ID로는 실패하고, 이메일로는 존재하는 사용자 정보 반환
        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(null)); // ID로 조회 실패
        when(memberFeignClient.findByEmail(oAuth2User.email())).thenReturn(ResponseEntity.ok(emailExistsResponse)); // 이메일로 조회 성공

        // When
        ResponseEntity<String> result = oAuth2UserService.login(oAuth2User, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("ALREADY_EXISTS", result.getBody()); // 이미 이메일이 존재하는 경우
    }

    @Test
    void testLogin_WhenUserIdAndEmailNotFound() {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");

        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(null));
        when(memberFeignClient.findByEmail(oAuth2User.email())).thenReturn(ResponseEntity.ok(null));

        // When
        ResponseEntity<String> result = oAuth2UserService.login(oAuth2User, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("NEED_REGISTER", result.getBody());
    }


    @Test
    void testLogin_WhenSuccessHandlerThrowsException() {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        MemberResponse memberResponse = new MemberResponse("user_id", "user_password", "USER", 123L, "ACTIVE");

        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(memberResponse));

        // Mocking to simulate an exception when saving RefreshToken
        doThrow(new RuntimeException("Database Error")).when(refreshTokenRepository).save(any(RefreshToken.class));

        // When & Then
        LoginFailedException exception = assertThrows(LoginFailedException.class,
                () -> oAuth2UserService.login(oAuth2User, response));
        assertEquals("Login SuccessHandler 중 실패", exception.getMessage());
    }

    @Test
    void testLogin_WhenUserAlreadyRegisteredAndSuccessHandlerSucceeds() {
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
        verify(jwtUtils, times(1)).createJwt(eq("Access"), eq("123"), eq("USER"), anyLong());
        verify(jwtUtils, times(1)).createJwt(eq("Refresh"), eq("123"), eq("USER"), anyLong());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testLogin_WhenUserAlreadyRegisteredAndSuccessHandlerFails() {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        MemberResponse memberResponse = new MemberResponse("user_id", "user_password", "USER", 123L, "ACTIVE");

        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(memberResponse));
        // Mock JwtUtils to throw an exception
        when(jwtUtils.createJwt(anyString(), anyString(), anyString(), anyLong())).thenThrow(new RuntimeException("Token creation failed"));

        // When & Then
        LoginFailedException exception = assertThrows(LoginFailedException.class,
                () -> oAuth2UserService.login(oAuth2User, response));
        assertEquals("Login SuccessHandler 중 실패", exception.getMessage());
    }


    @Test
    void testLogin_WhenSuccessHandlerThrowsRuntimeException() throws IOException {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        MemberResponse memberResponse = new MemberResponse("user_id", "password", "USER", 123L, "ACTIVE");
        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(memberResponse));

        // Mock RuntimeException in successHandler
        doThrow(new RuntimeException("Unexpected Error")).when(jwtUtils).createJwt(any(), any(), any(), anyLong());

        // When & Then
        LoginFailedException exception = assertThrows(LoginFailedException.class,
                () -> oAuth2UserService.login(oAuth2User, response));
        assertEquals("Login SuccessHandler 중 실패", exception.getMessage());
    }

    @Test
    void testLogin_WhenMemberResponseIsNull() {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(null));

        // When & Then
        ResponseEntity<String> responseEntity = oAuth2UserService.login(oAuth2User, response);
        assertEquals("NEED_REGISTER", responseEntity.getBody());
    }

    @Test
   void testLogin_WhenCustomerIdIsNull() {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        MemberResponse memberResponse = new MemberResponse("user_id", "password", "USER", null, "ACTIVE");
        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(memberResponse));

        // When & Then
        ResponseEntity<String> responseEntity = oAuth2UserService.login(oAuth2User, response);
        assertEquals("NEED_REGISTER", responseEntity.getBody());
    }

    @Test
    void testLogin_WhenMemberResponseIsValid() throws IOException {
        // Given
        OAuth2User oAuth2User = new OAuth2User("user_id", "user_email@test.com");
        MemberResponse memberResponse = new MemberResponse("user_id", "password", "USER", 123L, "ACTIVE");
        when(memberFeignClient.findByUsername(oAuth2User.id())).thenReturn(ResponseEntity.ok(memberResponse));

        // Mock response for successful processing
        doNothing().when(response).setHeader(anyString(), anyString());
        doNothing().when(response).addCookie(any());

        // When & Then
        ResponseEntity<String> responseEntity = oAuth2UserService.login(oAuth2User, response);
        assertEquals("LOGIN_SUCCESS", responseEntity.getBody());
    }
}
