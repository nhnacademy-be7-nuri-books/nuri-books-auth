package shop.nuribooks.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import shop.nuribooks.auth.dto.NonMemberRequest;
import shop.nuribooks.auth.dto.NonMemberResponse;
import shop.nuribooks.auth.service.NonMemberService;

@RequiredArgsConstructor
@RestController
public class NonMemberController {
	private final NonMemberService nonMemberService;

	@PostMapping("/api/auth/non-member/check")
	public ResponseEntity<NonMemberResponse> checkNonMember(@RequestBody NonMemberRequest nonMemberRequest) {
		return ResponseEntity.status(HttpStatus.OK).body(nonMemberService.checkNonMember(nonMemberRequest));
	}
}
