package shop.nuribooks.auth.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.dto.MemberResponse;
import shop.nuribooks.auth.dto.OAuth2User;
import shop.nuribooks.auth.entity.RefreshToken;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuth2UserService {
	private final MemberFeignClient memberFeignClient;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtUtils jwtUtils;

	public ResponseEntity<String> login(OAuth2User oAuth2User, HttpServletResponse response) {
		MemberResponse memberResponse = memberFeignClient.findByUsername(oAuth2User.id()).getBody();
		log.info("제공한 OAuth2User : {}", oAuth2User);
		log.info("ID로 조회한 memberResponse : {}", memberResponse);

		if (memberResponse.customerId() != null) {
			// OAuth2 ID가 이미 등록된 경우 => 로그인 처리 (토큰 발급)
			log.info("이미 등록된 아이디입니다 : {}", memberResponse.username());
			try {
				successHandler(memberResponse, response);
			} catch (IOException e) {
				throw new RuntimeException("Login SuccessHandler 중 실패");
			}
		} else {
			// OAuth2 ID가 존재하지 않는 경우 => OAuth2 정보 기반으로 간편 회원 가입
			memberResponse = memberFeignClient.findByEmail(oAuth2User.email()).getBody();
			if (memberResponse.customerId() != null) {
				// OAuth2로 가입하려는 email이 이미 등록된 경우 => 가입 불가
				log.info("해당 이메일이 이미 존재합니다 : {}", memberResponse.username());
				return ResponseEntity.status(HttpStatus.OK).body("ALREADY_EXISTS");
			} else {
				// OAuth2으로 최초 로그인 && 회원 DB에 없는 email => 가입
				log.info("가입 가능한 유저입니다!");
				return ResponseEntity.status(HttpStatus.OK).body("NEED_REGISTER");
			}
		}
		return ResponseEntity.status(HttpStatus.OK).body("LOGIN_SUCCESS");
	}

	private void successHandler(MemberResponse memberResponse, HttpServletResponse response) throws IOException {
		String userId = memberResponse.customerId().toString();
		String role = "ROLE_USER";

		String accessToken = jwtUtils.createJwt("Access", userId, role, JwtUtils.ACCESS_TOKEN_VALID_TIME);
		String refreshToken = jwtUtils.createJwt("Refresh", userId, role, JwtUtils.REFRESH_TOKEN_VALID_TIME);

		response.setHeader("Authorization", "Bearer " + accessToken);
		response.addCookie(CookieUtils.createCookie("Refresh", refreshToken, CookieUtils.REFRESH_TOKEN_MAX_AGE));
		addRefreshToken(userId, accessToken, refreshToken, JwtUtils.REFRESH_TOKEN_VALID_TIME);
		log.info("로그인 성공! Refresh Token을 저장하였습니다.");
	}

	private void addRefreshToken(String username, String accessToken, String refreshToken, Long expiredMs) {
		RefreshToken refresh = new RefreshToken();
		refresh.setUsername(username);
		refresh.setAccessToken(accessToken);
		refresh.setRefreshToken(refreshToken);
		refresh.setExpiration(new Date(System.currentTimeMillis() + expiredMs).toString());
		refreshTokenRepository.save(refresh);
	}
}
