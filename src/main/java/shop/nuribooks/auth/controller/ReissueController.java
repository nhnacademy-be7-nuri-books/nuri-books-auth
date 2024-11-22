package shop.nuribooks.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import shop.nuribooks.auth.service.ReissueService;

@RequiredArgsConstructor
@RestController
public class ReissueController {
	private final ReissueService reissueService;

	@PostMapping("/api/auth/reissue")
	public ResponseEntity<?> reissue(@RequestBody String refreshToken, HttpServletRequest request,
		HttpServletResponse response) {
		return reissueService.reissue(refreshToken, request, response);
	}
}
