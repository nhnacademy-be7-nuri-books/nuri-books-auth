package shop.nuribooks.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: API 테스트를 위한 임시 Controller, 추후 삭제 예정
@RestController
public class AdminController {
	@GetMapping("/admin")
	public String admin() {
		return "hello Admin";
	}
}
