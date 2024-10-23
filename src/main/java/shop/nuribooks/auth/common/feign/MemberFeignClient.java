package shop.nuribooks.auth.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import shop.nuribooks.auth.dto.MemberCreateRequest;
import shop.nuribooks.auth.dto.AuthorizedUser;

// TODO: 임시 API에 대하여 통신 중, 추후 url 및 API Spec에 따라 수정 예정
@FeignClient(name = "memberFeignClient", url = "http://localhost:9090/member")
public interface MemberFeignClient {
	@GetMapping("/{username}")
	AuthorizedUser findByUsername(@PathVariable("username") String username);

	@PostMapping("/register")
	void registerMember(@RequestBody MemberCreateRequest req);
}
