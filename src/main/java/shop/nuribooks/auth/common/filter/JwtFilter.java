package shop.nuribooks.auth.common.filter;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.util.JwtUtils;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtils jwtUtils;

	public JwtFilter(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		log.info("JwtFilter를 통과합니다.");

		// TODO: login 요청인 경우 토큰 유무에 관계 없이 바로 다음 필터로 보내는건 어떨까?
		String accessToken = request.getHeader("Authorization");

		// Access 토큰이 없는 경우
		if (accessToken == null || accessToken.isBlank() || !accessToken.startsWith("Bearer ")) {
			log.info("Access Token이 존재하지않습니다.");
			filterChain.doFilter(request, response);
			return;
		}

		String validAccessToken = accessToken.split(" ")[1];
		// Access 토큰이 만료된 경우
		if (jwtUtils.isExpired(validAccessToken)) {
			log.info("Access Token이 만료되었습니다. 재발급받으십시오.");
			filterChain.doFilter(request, response);
			return;
		}

		// Access 토큰이 유효한 경우 => 로그인 처리
		String username = jwtUtils.getUsername(validAccessToken);
		String role = jwtUtils.getRole(validAccessToken);
		UserDetails userDetails = new User(username, "p@ssW0rd", Collections.singleton(new SimpleGrantedAuthority(role)));
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		log.info("Access Token이 유효하여 자동 로그인 처리되었습니다. : {}/{}", userDetails.getUsername(), userDetails.getPassword());
		filterChain.doFilter(request, response);
	}
}
