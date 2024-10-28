package shop.nuribooks.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import shop.nuribooks.auth.service.AuthService;

@RequiredArgsConstructor
@RestController
// @Controller
public class AuthController {
	private final AuthService authService;

	@GetMapping("/")
	@ResponseBody
	public String home() {
		return "index page";
	}

	@GetMapping("/my")
	@ResponseBody
	public String my() {
		return "my page";
	}

	@GetMapping("/admin")
	@ResponseBody
	public String admin() {
		return "admin page";
	}

	@GetMapping("/auth/login")
	public String login() {
		return "login page";
	}

	@PostMapping("/reissue")
	public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
		return authService.reissue(request, response);
	}
}
