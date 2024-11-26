package shop.nuribooks.auth.service;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.exception.BadRequestException;
import shop.nuribooks.auth.common.exception.NotFoundException;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.entity.RefreshToken;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReissueService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtUtils jwtUtils;

	public ResponseEntity<Void> reissue(String refreshToken, HttpServletResponse response) {
		log.info("재발행을 위한 Refresh Token : {}", refreshToken);

		if (refreshToken == null || refreshToken.isBlank()) {
			log.info("Refresh Token is NULL");
			throw new BadRequestException("Refresh Token is NULL or Empty");
		}

		if (jwtUtils.isExpired(refreshToken)) {
			log.info("Refresh Token is EXPIRED");
			throw new BadRequestException("Refresh Token is Expired.");
		}

		RefreshToken findRefresh = refreshTokenRepository.findByRefresh(refreshToken);

		if (!refreshTokenRepository.existsByRefresh(refreshToken)) {
			log.info("The Refresh Token does not Exist");
			log.info("Find Refresh : {}", findRefresh);
			throw new NotFoundException("Refresh Token is NOT EXISTS");
		}

		String username = jwtUtils.getUsername(refreshToken);
		String role = jwtUtils.getRole(refreshToken);
		String newAccessToken = jwtUtils.createJwt("Access", username, role, JwtUtils.ACCESS_TOKEN_VALID_TIME);
		String newRefreshToken = jwtUtils.createJwt("Refresh", username, role, JwtUtils.REFRESH_TOKEN_VALID_TIME);
		response.setHeader("Authorization", newAccessToken);
		response.addCookie(CookieUtils.createCookie("Refresh", newRefreshToken, 60 * 60));
		refreshTokenRepository.deleteByRefresh(refreshToken);
		addRefreshToken(username, newAccessToken, newRefreshToken, 60 * 60 * 1000L * 24);
		log.info("Reissue Completed : Refresh Rotating, Saving New Refresh");
		log.info("새로 발급한 Access Token : {}", newAccessToken);
		log.info("새로 발급한 Refresh Token : {}", newRefreshToken);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void addRefreshToken(String username, String accessToken, String refreshToken, Long expiredMs) {
		RefreshToken refresh = new RefreshToken();
		refresh.setUsername(username);
		refresh.setAccess(accessToken);
		refresh.setRefresh(refreshToken);
		refresh.setExpiration(new Date(System.currentTimeMillis() + expiredMs).toString());
		refreshTokenRepository.save(refresh);
	}
}

