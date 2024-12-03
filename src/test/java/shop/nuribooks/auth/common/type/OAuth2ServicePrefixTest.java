package shop.nuribooks.auth.common.type;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OAuth2ServicePrefixTest {

    @Test
    void testNAVERPrefix() {
        // Given
        OAuth2ServicePrefix naverPrefix = OAuth2ServicePrefix.NAVER;

        // When
        String result = naverPrefix.toString();

        // Then
        assertEquals("NAVER-", result);
    }

    @Test
    void testPAYCOPrefix() {
        // Given
        OAuth2ServicePrefix paycoPrefix = OAuth2ServicePrefix.PAYCO;

        // When
        String result = paycoPrefix.toString();

        // Then
        assertEquals("PAYCO-", result);
    }

    @Test
    void testEnumValues() {
        // Given
        OAuth2ServicePrefix[] values = OAuth2ServicePrefix.values();

        // Then
        assertEquals(2, values.length);
        assertTrue(values[0] == OAuth2ServicePrefix.NAVER);
        assertTrue(values[1] == OAuth2ServicePrefix.PAYCO);
    }

    @Test
    void testEnumValueOf() {
        // Given
        OAuth2ServicePrefix naverPrefix = OAuth2ServicePrefix.valueOf("NAVER");
        OAuth2ServicePrefix paycoPrefix = OAuth2ServicePrefix.valueOf("PAYCO");

        // Then
        assertEquals(OAuth2ServicePrefix.NAVER, naverPrefix);
        assertEquals(OAuth2ServicePrefix.PAYCO, paycoPrefix);
    }
}
