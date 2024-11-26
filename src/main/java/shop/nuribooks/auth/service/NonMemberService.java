package shop.nuribooks.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import shop.nuribooks.auth.common.exception.NotFoundException;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.dto.NonMemberRequest;
import shop.nuribooks.auth.dto.NonMemberResponse;

@RequiredArgsConstructor
@Service
public class NonMemberService {
	private final MemberFeignClient memberFeignClient;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	public NonMemberResponse checkNonMember(NonMemberRequest nonMemberRequest) {
		try {
			NonMemberResponse nonMemberResponse = memberFeignClient.findNonMemberByEmail(nonMemberRequest.email())
				.getBody();

			boolean isValid = bCryptPasswordEncoder.matches(nonMemberRequest.password(), nonMemberResponse.password());
			if (isValid) {
				return nonMemberResponse;
			}
		} catch (FeignException ex) {
			throw new NotFoundException("찾을 수 없습니다.");
		}
		return null;
	}
}
