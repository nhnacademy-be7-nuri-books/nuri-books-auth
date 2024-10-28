package shop.nuribooks.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
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
		if (memberResponse != null) {
			return new CustomUserDetails(memberResponse);
		}
		throw new UsernameNotFoundException("회원 DB에 존재하지 않는 Username입니다.");
	}
}
