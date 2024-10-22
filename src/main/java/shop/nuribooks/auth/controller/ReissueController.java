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

@RestController
public class ReissueController {
	private final JwtUtils jwtUtils;

	@Autowired
	public ReissueController(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
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
		response.setHeader("access", newAccessToken);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
