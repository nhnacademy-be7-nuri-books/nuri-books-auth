package shop.nuribooks.auth.service;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	public ResponseEntity<Void> reissue(String refreshToken, HttpServletResponse response) {
		log.info("Reissuing tokens for Refresh Token: {}", refreshToken);

		if (refreshToken == null || refreshToken.isBlank()) {
			log.info("Refresh Token is NULL");
			throw new BadRequestException("Refresh Token is NULL or Empty");
		}

		if (jwtUtils.isExpired(refreshToken).booleanValue()) {
			log.info("Refresh Token is EXPIRED");
			throw new BadRequestException("Refresh Token is Expired.");
		}

		RefreshToken findRefresh = refreshTokenRepository.findByRefresh(refreshToken);
		if (findRefresh == null) {
			log.info("The Refresh Token does not Exist");
			throw new NotFoundException("Refresh Token is NOT EXISTS");
		}

		String username = jwtUtils.getUsername(refreshToken);
		String role = jwtUtils.getRole(refreshToken);

		String newAccessToken = jwtUtils.createJwt("Access", username, role, JwtUtils.ACCESS_TOKEN_VALID_TIME);
		String newRefreshToken = jwtUtils.createJwt("Refresh", username, role, JwtUtils.REFRESH_TOKEN_VALID_TIME);

		response.setHeader("Authorization", newAccessToken);
		response.addCookie(
			CookieUtils.createCookie("Refresh", newRefreshToken, (int)(JwtUtils.REFRESH_TOKEN_VALID_TIME / 1000)));

		findRefresh.setAccess(newAccessToken);
		findRefresh.setRefresh(newRefreshToken);
		findRefresh.setExpiration(Instant.now().plusMillis(JwtUtils.REFRESH_TOKEN_VALID_TIME).toString());
		log.info("Tokens reissued successfully: Access={}, Refresh={}", newAccessToken, newRefreshToken);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}

