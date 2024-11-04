package shop.nuribooks.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import shop.nuribooks.auth.service.AuthService;

@RequiredArgsConstructor
@RestController
public class AuthController {
	private final AuthService authService;
	
	@PostMapping("/api/auth/reissue")
	public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
		return authService.reissue(request, response);
	}
}
