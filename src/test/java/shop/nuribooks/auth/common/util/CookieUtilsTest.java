package shop.nuribooks.auth.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CookieUtilsTest {

    @Test
    void testCreateCookie() {
        // Given
        String key = "testKey";
        String value = "testValue";
        int maxAge = 3600;

        // When
        Cookie cookie = CookieUtils.createCookie(key, value, maxAge);

        // Then
        assertThat(cookie.getName()).isEqualTo(key);
        assertThat(cookie.getValue()).isEqualTo(value);
        assertThat(cookie.getMaxAge()).isEqualTo(maxAge);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    void testGetValueWhenCookieExists() {
        // Given
        String key = "testKey";
        String value = "testValue";
        Cookie[] cookies = {new Cookie(key, value)};

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);

        // When
        String result = CookieUtils.getValue(request, key);

        // Then
        assertThat(result).isEqualTo(value);
    }

    @Test
    void testGetValueWhenCookieDoesNotExist() {
        // Given
        String key = "testKey";
        Cookie[] cookies = {new Cookie("anotherKey", "anotherValue")};

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(cookies);

        // When
        String result = CookieUtils.getValue(request, key);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testGetValueWhenNoCookiesPresent() {
        // Given
        String key = "testKey";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        // When
        String result = CookieUtils.getValue(request, key);

        // Then
        assertThat(result).isNull();
    }
}
