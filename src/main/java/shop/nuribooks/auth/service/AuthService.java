package shop.nuribooks.auth.service;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.entity.RefreshToken;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtUtils jwtUtils;

	public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = CookieUtils.getValue(request, "Refresh");

		if (refreshToken == null || refreshToken.isBlank()) {
			log.info("Refresh Token is NULL");
			return new ResponseEntity<>("Refresh Token is NULL", HttpStatus.BAD_REQUEST);
		}

		if (jwtUtils.isExpired(refreshToken)) {
			log.info("Refresh Token is EXPIRED");
			return new ResponseEntity<>("Refresh Token is Expired", HttpStatus.BAD_REQUEST);
		}

		if (!refreshTokenRepository.existsByRefreshToken(refreshToken)) {
			log.info("Your Refresh Token NOT EXISTS");
			return new ResponseEntity<>("Refresh Token is NOT EXISTS", HttpStatus.NOT_FOUND);
		}

		String username = jwtUtils.getUsername(refreshToken);
		String role = jwtUtils.getRole(refreshToken);
		String newAccessToken = jwtUtils.createJwt("Access", username, role, 60 * 60 * 200L);
		String newRefreshToken = jwtUtils.createJwt("Refresh", username, role, 60 * 60 * 1000L * 24);
		response.setHeader("Authorization", newAccessToken);
		response.addCookie(CookieUtils.createCookie("Refresh", newRefreshToken, 60 * 60));
		refreshTokenRepository.deleteByRefreshToken(refreshToken);
		addRefreshToken(username, newAccessToken, newRefreshToken, 60 * 60 * 1000L * 24);
		log.info("Reissue Completed : Refresh Rotating, Saving New Refresh");
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void addRefreshToken(String username, String accessToken, String refreshToken, Long expiredMs) {
		RefreshToken refresh = new RefreshToken();
		refresh.setUsername(username);
		refresh.setAccessToken(accessToken);
		refresh.setRefreshToken(refreshToken);
		refresh.setExpiration(new Date(System.currentTimeMillis() + expiredMs).toString());
		refreshTokenRepository.save(refresh);
	}
}

