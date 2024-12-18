package shop.nuribooks.auth.common.filter;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.exception.InactiveUserFoundException;
import shop.nuribooks.auth.common.exception.LoginFailedException;
import shop.nuribooks.auth.common.feign.MemberFeignClient;
import shop.nuribooks.auth.common.message.ErrorResponse;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.LoginRequest;
import shop.nuribooks.auth.dto.StatusType;
import shop.nuribooks.auth.entity.RefreshToken;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

@Slf4j
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;
	private final RefreshTokenRepository refreshTokenRepository;
	private final MemberFeignClient memberFeignClient;
	private final JwtUtils jwtUtils;

	public CustomLoginFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
		RefreshTokenRepository refreshTokenRepository, MemberFeignClient memberFeignClient) {
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
		this.refreshTokenRepository = refreshTokenRepository;
		this.memberFeignClient = memberFeignClient;
		setFilterProcessesUrl("/api/auth/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		ObjectMapper objectMapper = new ObjectMapper();
		LoginRequest loginRequest = null;
		try {
			loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
		} catch (IOException e) {
			log.info("Fail to convert to Login Request.");
			throw new LoginFailedException("Fail to convert to Login Request.");
		}

		String username = loginRequest.username();
		String password = loginRequest.password();
		log.info("로그인 시도 : {}/{}", username, password);

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		return authenticationManager.authenticate(token);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authentication) throws IOException, ServletException {
		CustomUserDetails userDetails = (CustomUserDetails)authentication.getPrincipal();

		if (userDetails.getStatus() == StatusType.INACTIVE) {
			unsuccessfulAuthentication(request, response, new InactiveUserFoundException("해당 아이디는 휴면 계정입니다."));
			return;
		}

		String userId = userDetails.getUserId();
		String role = userDetails.getAuthorities().iterator().next().getAuthority();

		String accessToken = jwtUtils.createJwt("Access", userId, role, JwtUtils.ACCESS_TOKEN_VALID_TIME);
		String refreshToken = jwtUtils.createJwt("Refresh", userId, role, JwtUtils.REFRESH_TOKEN_VALID_TIME);

		response.setHeader("Authorization", "Bearer " + accessToken);
		response.addCookie(
			CookieUtils.createCookie("Refresh", refreshToken, (int)(JwtUtils.REFRESH_TOKEN_VALID_TIME / 1000)));
		addRefreshToken(userId, accessToken, refreshToken, JwtUtils.REFRESH_TOKEN_VALID_TIME);
		log.info("로그인 성공 : ({}/enabled: {}), Refresh Token을 저장하였습니다.", userDetails.getUserId(),
			userDetails.isEnabled());

		ResponseEntity<String> responseEntity = ResponseEntity.ok("{\"message\":\"Login successful.\"}");
		memberFeignClient.informLogin(userDetails.getUsername());
		response.setStatus(responseEntity.getStatusCode().value());
		response.getWriter().write(responseEntity.getBody());
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		log.info("로그인 실패 : {}", "아이디 또는 비밀번호를 확인하세요");
		ErrorResponse errorResponse = new ErrorResponse(
			HttpServletResponse.SC_UNAUTHORIZED,
			"아이디 또는 비밀번호를 확인하세요",
			request.getRequestURI()
		);

		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(errorResponse.statusCode());
		response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
	}

	private void addRefreshToken(String username, String accessToken, String refreshToken, Long expiredMs) {
		RefreshToken refresh = new RefreshToken();
		refresh.setUsername(username);
		refresh.setAccess(accessToken);
		refresh.setRefresh(refreshToken);
		refresh.setExpiration(Instant.now().plusMillis(expiredMs).toString());
		refreshTokenRepository.save(refresh);
	}
}
