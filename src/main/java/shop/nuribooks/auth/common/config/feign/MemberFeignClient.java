package shop.nuribooks.auth.common.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import shop.nuribooks.auth.dto.MemberCreateReq;
import shop.nuribooks.auth.dto.MemberRes;

@FeignClient(name = "memberFeignClient", url = "http://localhost:9090/member")
public interface MemberFeignClient {
	@GetMapping("/{username}")
	MemberRes findByUsername(@PathVariable("username") String username);

	@PostMapping("/register")
	void registerMember(@RequestBody MemberCreateReq req);
}
