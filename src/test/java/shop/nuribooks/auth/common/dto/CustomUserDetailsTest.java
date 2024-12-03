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
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        boolean accountNonExpired = userDetails.isAccountNonExpired();

        // Then
        assertTrue(accountNonExpired);
    }

    @Test
    void testIsAccountNonLocked() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        boolean accountNonLocked = userDetails.isAccountNonLocked();

        // Then
        assertTrue(accountNonLocked);
    }

    @Test
    void testIsCredentialsNonExpired() {
        // Given
        MemberResponse mockResponse = Mockito.mock(MemberResponse.class);
        CustomUserDetails userDetails = new CustomUserDetails(mockResponse);

        // When
        boolean credentialsNonExpired = userDetails.isCredentialsNonExpired();

        // Then
        assertTrue(credentialsNonExpired);
    }
}
