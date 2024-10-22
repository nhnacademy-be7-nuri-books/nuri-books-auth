package shop.nuribooks.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.AuthorizedUser;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final MemberFeignClient memberFeignClient;

	public CustomUserDetailsService(MemberFeignClient memberFeignClient) {
		this.memberFeignClient = memberFeignClient;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AuthorizedUser member = memberFeignClient.findByUsername(username);
		if (member != null) {
			return new CustomUserDetails(member);
		}
		return null;
	}
}
