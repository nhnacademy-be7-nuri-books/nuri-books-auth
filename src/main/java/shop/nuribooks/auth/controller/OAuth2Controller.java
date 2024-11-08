package shop.nuribooks.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.dto.OAuth2User;
import shop.nuribooks.auth.service.OAuth2UserService;

@Slf4j
@RequiredArgsConstructor
@RestController
public class OAuth2Controller {
	private final OAuth2UserService oAuth2UserService;

	@PostMapping("/api/auth/oauth2")
	public ResponseEntity<String> login(@RequestBody OAuth2User oAuth2User, HttpServletResponse response) {
		return oAuth2UserService.login(oAuth2User, response);
	}
}
