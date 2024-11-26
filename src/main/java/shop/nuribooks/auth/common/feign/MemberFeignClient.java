package shop.nuribooks.auth.common.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import shop.nuribooks.auth.dto.MemberResponse;
import shop.nuribooks.auth.dto.NonMemberResponse;

@FeignClient(name = "memberFeignClient", url = "http://localhost:8083")
public interface MemberFeignClient {
	@GetMapping("/api/members/username/{username}")
	ResponseEntity<MemberResponse> findByUsername(@PathVariable String username);

	@GetMapping("/api/members/email/{email}")
	ResponseEntity<MemberResponse> findByEmail(@PathVariable String email);

	@PutMapping("/api/members/{username}/login-time")
	ResponseEntity<Void> informLogin(@PathVariable String username);

	@GetMapping("/api/members/customers/{email}")
	ResponseEntity<NonMemberResponse> findNonMemberByEmail(@PathVariable String email);
}
