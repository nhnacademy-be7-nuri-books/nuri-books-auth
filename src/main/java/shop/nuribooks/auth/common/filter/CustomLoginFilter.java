package shop.nuribooks.auth.common.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.common.util.RefreshUtils;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.LoginReq;
import shop.nuribooks.auth.repository.RefreshRepository;

public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {
	private final AuthenticationManager authenticationManager;
	private final JwtUtils jwtUtils;
	private final RefreshRepository refreshRepository;

	public CustomLoginFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
		RefreshRepository refreshRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtUtils = jwtUtils;
		this.refreshRepository = refreshRepository;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		LoginReq loginReq = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			ServletInputStream in = request.getInputStream();
			String messageBody = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
			loginReq = objectMapper.readValue(messageBody, LoginReq.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		String username = loginReq.getUsername();
		String password = loginReq.getPassword();
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		return authenticationManager.authenticate(token);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authentication) throws IOException, ServletException {
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		String username = userDetails.getUsername();
		String role = userDetails.getAuthorities().iterator().next().getAuthority();

		String accessToken = jwtUtils.createJwt("access", username, role, 60 * 60 * 1000L); 		// 1 hour
		String refreshToken = jwtUtils.createJwt("refresh", username, role, 60 * 60 * 3000L);	// 3 hour
		response.addHeader("access", accessToken);
		response.addCookie(CookieUtils.createCookie("refresh", refreshToken));
		RefreshUtils.addRefreshToken(refreshRepository, username, refreshToken, 60 * 60 * 3000L);
		response.setStatus(HttpStatus.OK.value());
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		response.setStatus(401);
	}
}
