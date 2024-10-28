package shop.nuribooks.auth.common.filter;

import java.io.IOException;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
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
		// form 요청 기반
		// String username = obtainUsername(request);
		// String password = obtainPassword(request);



		// application/json 요청 기반
		ObjectMapper objectMapper = new ObjectMapper();
		LoginRequest loginRequest = null;
		try {
			 loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
		} catch (Exception e) {
			log.info("로그인 요청 정보를 가져오는데 실패하였습니다.");
			return null;
		}

		String username = loginRequest.getUsername();
		String password = loginRequest.getPassword();

		log.info("로그인 시도 : {}/{}", username, password);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password, null);
		return authenticationManager.authenticate(token);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authentication) throws IOException, ServletException {

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
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
		response.setContentType("application/json");
		response.getWriter().write("{\"message\":\"Login successful\"}");
		response.setStatus(HttpStatus.OK.value());
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		log.info("로그인 실패");
		// JSON 응답을 반환하도록 변경
		response.setContentType("application/json");
		response.getWriter().write("{\"message\":\"Login failed\"}");
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
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
