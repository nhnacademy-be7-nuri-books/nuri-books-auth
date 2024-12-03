package shop.nuribooks.auth.common.feign;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import shop.nuribooks.auth.dto.MemberResponse;
import shop.nuribooks.auth.dto.NonMemberResponse;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class MemberFeignClientTest {

    @MockBean
    private MemberFeignClient memberFeignClient;

    @Test
    void testFindByUsername() {
        // Given
        String username = "testUser";
        MemberResponse mockMemberResponse = new MemberResponse(
                username, "password123", "USER", 1L, "ACTIVE"
        );
        when(memberFeignClient.findByUsername(username)).thenReturn(ResponseEntity.ok(mockMemberResponse));

        // When
        ResponseEntity<MemberResponse> response = memberFeignClient.findByUsername(username);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().username()).isEqualTo(username);
        assertThat(response.getBody().password()).isEqualTo("password123");
        assertThat(response.getBody().role()).isEqualTo("USER");
        assertThat(response.getBody().customerId()).isEqualTo(1L);
        assertThat(response.getBody().status()).isEqualTo("ACTIVE");

        // Verify that the Feign client method was called
        verify(memberFeignClient, times(1)).findByUsername(username);
    }

    @Test
    void testFindByEmail() {
        // Given
        String email = "test@example.com";
        MemberResponse mockMemberResponse = new MemberResponse(
                "testUser", "password123", "USER", 1L, "ACTIVE"
        );
        when(memberFeignClient.findByEmail(email)).thenReturn(ResponseEntity.ok(mockMemberResponse));

        // When
        ResponseEntity<MemberResponse> response = memberFeignClient.findByEmail(email);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().username()).isEqualTo("testUser");
        assertThat(response.getBody().password()).isEqualTo("password123");
        assertThat(response.getBody().role()).isEqualTo("USER");
        assertThat(response.getBody().customerId()).isEqualTo(1L);
        assertThat(response.getBody().status()).isEqualTo("ACTIVE");

        // Verify that the Feign client method was called
        verify(memberFeignClient, times(1)).findByEmail(email);
    }

    @Test
    void testInformLogin() {
        // Given
        String username = "testUser";

        // void 메서드를 mocking하기 위해 doNothing() 사용
        when(memberFeignClient.informLogin(username)).thenReturn(null);

        // When
        memberFeignClient.informLogin(username);

        // Then
        // Feign client 메서드가 호출되었는지 확인
        verify(memberFeignClient, times(1)).informLogin(username);
    }


    @Test
    void testFindNonMemberByEmail() {
        // Given
        String email = "nonmember@example.com";
        NonMemberResponse mockNonMemberResponse = new NonMemberResponse(
                1L, email, "nonmemberpassword"
        );
        when(memberFeignClient.findNonMemberByEmail(email)).thenReturn(ResponseEntity.ok(mockNonMemberResponse));

        // When
        ResponseEntity<NonMemberResponse> response = memberFeignClient.findNonMemberByEmail(email);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().customerId()).isEqualTo(1L);
        assertThat(response.getBody().email()).isEqualTo(email);
        assertThat(response.getBody().password()).isEqualTo("nonmemberpassword");

        // Verify that the Feign client method was called
        verify(memberFeignClient, times(1)).findNonMemberByEmail(email);
    }
}
