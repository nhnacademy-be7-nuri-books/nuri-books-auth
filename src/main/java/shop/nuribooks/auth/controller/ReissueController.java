package shop.nuribooks.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.common.util.RefreshUtils;
import shop.nuribooks.auth.repository.RefreshRepository;

// TODO: Token 재발행을 위한 Controller로 front와 정확한 Spec을 공유하여 수정 예정
@RestController
public class ReissueController {
	private final JwtUtils jwtUtils;
	private final RefreshRepository refreshRepository;

	@Autowired
	public ReissueController(JwtUtils jwtUtils, RefreshRepository refreshRepository) {
		this.jwtUtils = jwtUtils;
		this.refreshRepository = refreshRepository;
	}

	@PostMapping("/reissue")
	public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = CookieUtils.getValue(request, "refresh");
		if (refreshToken == null) {
			return new ResponseEntity<>("Refresh Token is NULL", HttpStatus.BAD_REQUEST);
		}

		try {
			jwtUtils.isExpired(refreshToken);
		} catch (ExpiredJwtException ex) {
			return new ResponseEntity<>("Refresh Token is EXPIRED", HttpStatus.BAD_REQUEST);
		}

		String tokenCategory = jwtUtils.getCategory(refreshToken);
		if (!tokenCategory.equals("refresh")) {
			return new ResponseEntity<>("Refresh Token is INVALID", HttpStatus.BAD_REQUEST);
		}

		String username = jwtUtils.getUsername(refreshToken);
		String role = jwtUtils.getRole(refreshToken);
		String newAccessToken = jwtUtils.createJwt("access", username, role, 60 * 60 * 1000L);
		String newRefreshToken = jwtUtils.createJwt("refresh", username, role, 60 * 60 * 3000L);

		response.setHeader("access", newAccessToken);
		response.addCookie(CookieUtils.createCookie("refresh", newRefreshToken));
		RefreshUtils.reissueOnDb(refreshRepository, username, refreshToken, newRefreshToken, 60 * 60 * 3000L);

		return new ResponseEntity<>(HttpStatus.OK);
	}
}