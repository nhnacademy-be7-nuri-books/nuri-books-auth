package shop.nuribooks.auth.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import shop.nuribooks.auth.dto.MemberResponse;

@FeignClient(name = "memberFeignClient", url = "http://localhost:8083/api/member")
public interface MemberFeignClient {
	@GetMapping("/{username}")
	ResponseEntity<MemberResponse> findByUsername(@PathVariable String username);
}
