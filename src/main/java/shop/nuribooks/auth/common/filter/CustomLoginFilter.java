package shop.nuribooks.auth.common.filter;

import java.io.IOException;
import java.util.Date;

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
import shop.nuribooks.auth.common.message.ErrorResponse;
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
		ObjectMapper objectMapper = new ObjectMapper();
		LoginRequest loginRequest = null;
		try {
			loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
		} catch (IOException e) {
			log.info("Fail to convert to Login Request.");
			throw new AuthenticationException("Fail to convert to Login Request.") {};
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
		String userId = userDetails.getUserId();
		String role = userDetails.getAuthorities().iterator().next().getAuthority();

		String accessToken = jwtUtils.createJwt("Access", userId, role, JwtUtils.ACCESS_TOKEN_VALID_TIME);
		String refreshToken = jwtUtils.createJwt("Refresh", userId, role, JwtUtils.REFRESH_TOKEN_VALID_TIME);

		response.setHeader("Authorization", "Bearer " + accessToken);
		response.addCookie(CookieUtils.createCookie("Refresh", refreshToken, CookieUtils.REFRESH_TOKEN_MAX_AGE));
		addRefreshToken(userId, accessToken, refreshToken, JwtUtils.REFRESH_TOKEN_VALID_TIME);
		log.info("로그인 성공! Refresh Token을 저장하였습니다.");

		ResponseEntity<String> responseEntity = ResponseEntity.ok("{\"message\":\"Login successful.\"}");
		response.setStatus(responseEntity.getStatusCodeValue());
		response.getWriter().write(responseEntity.getBody());
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		log.info("로그인 실패");
		ErrorResponse errorResponse = new ErrorResponse(
			HttpServletResponse.SC_UNAUTHORIZED,
			"Login failed.",
			request.getRequestURI()
		);

		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(errorResponse.statusCode());
		response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
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
