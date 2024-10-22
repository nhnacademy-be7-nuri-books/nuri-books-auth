package shop.nuribooks.auth.common.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "memberFeignClient", url = "http://localhost:8080/member")
public interface MemberFeignClient {
	@GetMapping("/{username}")
	String findByUsername(@PathVariable("username") String username);
}
