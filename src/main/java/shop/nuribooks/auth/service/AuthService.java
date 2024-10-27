package shop.nuribooks.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
	private final JwtUtils jwtUtils;

	public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
		String refreshToken = CookieUtils.getValue(request, "Refresh");

		if (refreshToken == null) {
			log.info("Refresh Token is NULL");
			return new ResponseEntity<>("Refresh Token is NULL", HttpStatus.BAD_REQUEST);
		}

		if (jwtUtils.isExpired(refreshToken)) {
			log.info("Refresh Token is EXPIRED");
			return new ResponseEntity<>("Refresh Token is Expired", HttpStatus.BAD_REQUEST);
		}

		String username = jwtUtils.getUsername(refreshToken);
		String role = jwtUtils.getRole(refreshToken);
		String newAccessToken = jwtUtils.createJwt("Access", username, role, 60 * 60 * 200L);
		response.setHeader("Authorization", newAccessToken);
		log.info("Reissue Completed : {}", newAccessToken);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
