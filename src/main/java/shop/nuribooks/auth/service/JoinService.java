package shop.nuribooks.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.dto.MemberCreateReq;
import shop.nuribooks.auth.dto.AuthorizedUser;

@Service
public class JoinService {
	private final MemberFeignClient memberFeignClient;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	public JoinService(MemberFeignClient memberFeignClient, BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.memberFeignClient = memberFeignClient;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}

	public void join(MemberCreateReq req) {
		AuthorizedUser member = memberFeignClient.findByUsername(req.getUsername());
		if (member != null) {
			return;
		}
		req.setPassword(bCryptPasswordEncoder.encode(req.getPassword()));
		memberFeignClient.registerMember(req);
	}
}
