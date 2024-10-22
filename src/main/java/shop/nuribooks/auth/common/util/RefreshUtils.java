package shop.nuribooks.auth.common.util;

import java.util.Date;

import shop.nuribooks.auth.entity.RefreshToken;
import shop.nuribooks.auth.repository.RefreshRepository;

public class RefreshUtils {
	public static void addRefreshToken(RefreshRepository refreshRepository, String username, String refresh, Long expiredMs) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUsername(username);
		refreshToken.setRefresh(refresh);
		refreshToken.setExpiration((new Date(System.currentTimeMillis() + expiredMs)).toString());
		refreshRepository.save(refreshToken);
	}

	public static boolean existByRefreshToken(RefreshRepository refreshRepository, String refresh) {
		return refreshRepository.existsByRefresh(refresh);
	}

	public static void reissueOnDb(RefreshRepository refreshRepository, String username, String oldRefresh, String newRefresh, Long expiredMs) {
		refreshRepository.deleteByRefresh(oldRefresh);
		addRefreshToken(refreshRepository, username, newRefresh, expiredMs);
	}
}
