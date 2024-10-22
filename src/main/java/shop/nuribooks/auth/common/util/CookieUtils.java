package shop.nuribooks.auth.common.util;

import jakarta.servlet.http.Cookie;

public class CookieUtils {
	public static Cookie createCookie(String key, String value) {
		Cookie cookie = new Cookie(key, value);
		cookie.setPath("/");
		// cookie.setSecure(true);
		cookie.setHttpOnly(true);
		cookie.setMaxAge(60 * 60);
		return cookie;
	}
}
