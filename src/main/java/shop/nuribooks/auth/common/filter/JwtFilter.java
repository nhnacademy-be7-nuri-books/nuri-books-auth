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
		String accessToken = request.getHeader("Authorization");

		if (accessToken == null || !accessToken.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String validAccessToken = accessToken.split(" ")[1];
		if (jwtUtils.isExpired(validAccessToken).booleanValue()) {
			filterChain.doFilter(request, response);
			return;
		}

		String username = jwtUtils.getUsername(validAccessToken);
		String role = jwtUtils.getRole(validAccessToken);
		if (username == null || role == null) {
			filterChain.doFilter(request, response);
			return;
		}

		UserDetails userDetails = new User(username, "", Collections.singleton(new SimpleGrantedAuthority(role)));
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
}
