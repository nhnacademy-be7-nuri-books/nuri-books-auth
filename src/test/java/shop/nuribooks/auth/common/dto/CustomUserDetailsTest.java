package shop.nuribooks.auth.common.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.GrantedAuthority;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.MemberResponse;
import shop.nuribooks.auth.dto.StatusType;

class CustomUserDetailsTest {

    @Test
    void testGetAuthorities() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        Mockito.when(mockResponse.role()).thenReturn("ROLE_USER");
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        // Then
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void testGetUsername() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        Mockito.when(mockResponse.username()).thenReturn("testUser");
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        String username = userDetails.getUsername();

        // Then
        assertEquals("testUser", username);
    }

    @Test
    void testGetPassword() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        Mockito.when(mockResponse.password()).thenReturn("encryptedPassword");
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        String password = userDetails.getPassword();

        // Then
        assertEquals("encryptedPassword", password);
    }

    @Test
    void testIsEnabled_WithActiveStatus() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        Mockito.when(mockResponse.status()).thenReturn("STATUS_" + StatusType.ACTIVE.getValue());
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        boolean isEnabled = userDetails.isEnabled();

        // Then
        assertTrue(isEnabled);
    }

    @Test
    void testIsEnabled_WithWithdrawnStatus() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        Mockito.when(mockResponse.status()).thenReturn("STATUS_" + StatusType.WITHDRAWN.getValue());
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        boolean isEnabled = userDetails.isEnabled();

        // Then
        assertFalse(isEnabled);
    }

    @Test
    void testGetUserId() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        Mockito.when(mockResponse.customerId()).thenReturn(123L);
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        String userId = userDetails.getUserId();

        // Then
        assertEquals("123", userId);
    }

    @Test
    void testGetStatus() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        Mockito.when(mockResponse.status()).thenReturn("STATUS_" + StatusType.INACTIVE.getValue());
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        StatusType status = userDetails.getStatus();

        // Then
        assertEquals(StatusType.INACTIVE, status);
    }

    @Test
    void testIsAccountNonExpired() {
        // Given
        MemberResponse mockUser = new MemberResponse("testUser", "password123", "USER", 1L, "ACTIVE");
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        // When
        boolean result = userDetails.isAccountNonExpired();

        // Then
        assertTrue(result, "Account should always be non-expired");
    }

    @Test
    void testIsAccountNonLocked() {
        // Given
        MemberResponse mockUser = new MemberResponse("testUser", "password123", "USER", 1L, "ACTIVE");
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        // When
        boolean result = userDetails.isAccountNonLocked();

        // Then
        assertTrue(result, "Account should always be non-locked");
    }

    @Test
    void testIsCredentialsNonExpired() {
        // Given
        MemberResponse mockUser = new MemberResponse("testUser", "password123", "USER", 1L, "ACTIVE");
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        // When
        boolean result = userDetails.isCredentialsNonExpired();

        // Then
        assertTrue(result, "Credentials should always be non-expired");
    }
}
