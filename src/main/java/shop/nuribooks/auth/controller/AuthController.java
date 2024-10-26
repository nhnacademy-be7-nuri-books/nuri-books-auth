package shop.nuribooks.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
	@GetMapping("/")
	public String home() {
		return "Hello Index";
	}

	@GetMapping("/my")
	public String my() {
		return "my!";
	}

	@GetMapping("/admin")
	public String admin() {
		return "Hello ADMIN";
	}
}
