package shop.nuribooks.auth.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import shop.nuribooks.auth.common.exception.InactiveUserFoundException;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.MemberResponse;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private MemberFeignClient memberFeignClient;

    private MemberResponse mockMemberResponse;

    @BeforeEach
    void setUp() {
        mockMemberResponse = new MemberResponse("testUsername", "password", "USER", 123L, "ACTIVE");
    }

    @Test
    void testLoadUserByUsername_success() {
        // given
        String username = "testUsername";
        when(memberFeignClient.findByUsername(username)).thenReturn(ResponseEntity.ok(mockMemberResponse));

        // when
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(username);

        // then
        assertNotNull(userDetails);
        assertEquals(mockMemberResponse.username(), userDetails.getUsername());
        assertEquals(mockMemberResponse.role(), userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void testLoadUserByUsername_notFound() {
        // given
        String username = "nonExistentUsername";
        when(memberFeignClient.findByUsername(username)).thenReturn(ResponseEntity.ok(null));

        // when & then
        assertThrows(InactiveUserFoundException.class, () -> customUserDetailsService.loadUserByUsername(username));
    }

    @Test
    void testLoadUserByUsername_inactiveUser() {
        // given
        String username = "testUsername";
        MemberResponse inactiveMemberResponse = new MemberResponse("testUsername", "password", "USER", 123L, "INACTIVE");
        when(memberFeignClient.findByUsername(username)).thenReturn(ResponseEntity.ok(inactiveMemberResponse));

        // when & then
        assertThrows(InactiveUserFoundException.class, () -> customUserDetailsService.loadUserByUsername(username));
    }

    @Test
    public void testLoadUserByUsername_WhenMemberResponseIsNull() {
        // Given
        String username = "test_user";
        when(memberFeignClient.findByUsername(username)).thenReturn(ResponseEntity.ok(null));

        // When & Then
        InactiveUserFoundException exception = assertThrows(InactiveUserFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username));
        assertEquals("아이디 또는 비밀번호를 확인하세요", exception.getMessage());
    }

    @Test
    public void testLoadUserByUsername_WhenMemberResponseUsernameIsNull() {
        // Given
        String username = "test_user";
        MemberResponse memberResponse = new MemberResponse(null, "password", "USER", 123L, "ACTIVE");
        when(memberFeignClient.findByUsername(username)).thenReturn(ResponseEntity.ok(memberResponse));

        // When & Then
        InactiveUserFoundException exception = assertThrows(InactiveUserFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username));
        assertEquals("아이디 또는 비밀번호를 확인하세요", exception.getMessage());
    }
}
