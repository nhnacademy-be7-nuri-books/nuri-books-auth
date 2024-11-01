package shop.nuribooks.auth.common.filter;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.LoginRequest;
import shop.nuribooks.auth.entity.RefreshToken;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

@Slf4j
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtUtils jwtUtils;

	public CustomLoginFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils, RefreshTokenRepository refreshTokenRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
		this.refreshTokenRepository = refreshTokenRepository;
		setFilterProcessesUrl("/api/auth/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		// application/json 요청 기반
		ObjectMapper objectMapper = new ObjectMapper();
		LoginRequest loginRequest = null;
		try {
			 loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
		} catch (Exception e) {
			log.info("로그인 요청 정보를 가져오는데 실패하였습니다.");
			return null;
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
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		String username = userDetails.getUsername();
		String role = userDetails.getAuthorities().iterator().next().getAuthority();

		String accessToken = jwtUtils.createJwt("Access", username, role, 60 * 60 * 200L);
		String refreshToken = jwtUtils.createJwt("Refresh", username, role, 60 * 60 * 1000L * 24);

		response.setHeader("Authorization", "Bearer " + accessToken);
		response.addCookie(CookieUtils.createCookie("Refresh", refreshToken, 60 * 60));
		addRefreshToken(username, accessToken, refreshToken, 60 * 60 * 1000L * 24);
		log.info("로그인 성공! Refresh Token을 저장하였습니다.");

		// TODO: login 요청 성공 후
		// JSON 응답을 반환하도록 변경
		ResponseEntity<String> responseEntity = ResponseEntity.ok("{\"message\":\"Login successful\"}");
		response.setStatus(responseEntity.getStatusCodeValue());
		response.getWriter().write(responseEntity.getBody());
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		log.info("로그인 실패");
		// ResponseEntity를 사용하여 JSON 응답 생성
		ResponseEntity<String> responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body("{\"message\":\"Login failed\"}");
		response.setStatus(responseEntity.getStatusCodeValue());
		response.getWriter().write(responseEntity.getBody());
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
