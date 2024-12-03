package shop.nuribooks.auth.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.ResponseEntity;
import shop.nuribooks.auth.common.exception.NotFoundException;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.dto.NonMemberRequest;
import shop.nuribooks.auth.dto.NonMemberResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NonMemberServiceTest {

    @Mock
    private MemberFeignClient memberFeignClient;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private NonMemberService nonMemberService;

    private NonMemberRequest nonMemberRequest;
    private NonMemberResponse nonMemberResponse;

    @BeforeEach
    void setUp() {
        // Mockito mock 객체 초기화
        MockitoAnnotations.openMocks(this);

        nonMemberRequest = new NonMemberRequest("test@example.com", "password123");
        nonMemberResponse = new NonMemberResponse(1L, "test@example.com", "encodedPassword123");

        // 암호 비교를 위한 bcrypt 모의 설정
        when(bCryptPasswordEncoder.matches(nonMemberRequest.password(), nonMemberResponse.password()))
                .thenReturn(true); // 비밀번호가 일치한다고 가정
    }

    @Test
    void checkNonMember_ShouldReturnNonMemberResponse_WhenCredentialsAreValid() {
        // given
        when(memberFeignClient.findNonMemberByEmail(nonMemberRequest.email()))
                .thenReturn(ResponseEntity.ok(nonMemberResponse));

        // when
        NonMemberResponse result = nonMemberService.checkNonMember(nonMemberRequest);

        // then
        assertNotNull(result);
        assertEquals(nonMemberRequest.email(), result.email());
    }

    @Test
    void checkNonMember_ShouldReturnNull_WhenCredentialsAreInvalid() {
        // given
        when(memberFeignClient.findNonMemberByEmail(nonMemberRequest.email()))
                .thenReturn(ResponseEntity.ok(nonMemberResponse));
        when(bCryptPasswordEncoder.matches(nonMemberRequest.password(), nonMemberResponse.password()))
                .thenReturn(false); // 비밀번호가 일치하지 않음

        // when
        NonMemberResponse result = nonMemberService.checkNonMember(nonMemberRequest);

        // then
        assertNull(result);
    }

    @Test
    void checkNonMember_ShouldThrowNotFoundException_WhenFeignExceptionOccurs() {
        // given
        when(memberFeignClient.findNonMemberByEmail(nonMemberRequest.email()))
                .thenThrow(FeignException.class); // Feign 호출 시 예외 발생

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            nonMemberService.checkNonMember(nonMemberRequest);
        });
        assertEquals("찾을 수 없습니다.", exception.getMessage());
    }
}
