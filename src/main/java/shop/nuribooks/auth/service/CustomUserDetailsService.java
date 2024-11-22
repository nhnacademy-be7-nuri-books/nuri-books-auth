package shop.nuribooks.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import shop.nuribooks.auth.common.exception.InactiveUserFoundException;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.MemberResponse;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final MemberFeignClient memberFeignClient;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MemberResponse memberResponse = memberFeignClient.findByUsername(username).getBody();

		if (memberResponse.username() == null) {
			throw new InactiveUserFoundException("아이디 또는 비밀번호를 확인하세요");
		}

		return new CustomUserDetails(memberResponse);
	}
}
