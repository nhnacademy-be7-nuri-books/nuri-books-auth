package shop.nuribooks.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import shop.nuribooks.auth.dto.MemberCreateRequest;
import shop.nuribooks.auth.service.JoinService;

// TODO: API 테스트를 위한 임시 Controller, 추후 삭제 예정
@RestController
public class JoinController {
	private final JoinService joinService;

	@Autowired
	public JoinController(JoinService joinService) {
		this.joinService = joinService;
	}

	@PostMapping("/api/join")
	public String join(@RequestBody MemberCreateRequest req) {
		joinService.join(req);
		return "ok";
	}
}
