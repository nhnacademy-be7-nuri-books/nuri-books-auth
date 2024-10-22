package shop.nuribooks.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import shop.nuribooks.auth.dto.MemberCreateReq;
import shop.nuribooks.auth.service.JoinService;

@RestController
public class JoinController {
	private final JoinService joinService;

	@Autowired
	public JoinController(JoinService joinService) {
		this.joinService = joinService;
	}

	@PostMapping("/join")
	public String join(@RequestBody MemberCreateReq req) {
		joinService.join(req);
		return "ok";
	}
}
