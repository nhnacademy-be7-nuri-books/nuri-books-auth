package shop.nuribooks.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import shop.nuribooks.auth.common.exception.InactiveUserFoundException;
import shop.nuribooks.auth.common.exception.NotFoundException;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.MemberResponse;
import shop.nuribooks.auth.dto.StatusType;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
	private final MemberFeignClient memberFeignClient;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		MemberResponse memberResponse = memberFeignClient.findByUsername(username).getBody();

		if (memberResponse == null) {
			throw new NotFoundException("회원 DB에 존재하지 않는 username입니다.");
		}

		if (StatusType.fromValue(memberResponse.status()) == StatusType.INACTIVE) {
			throw new InactiveUserFoundException("휴면 회원을 조회하였습니다.");
		}

		return new CustomUserDetails(memberResponse);
	}
}
