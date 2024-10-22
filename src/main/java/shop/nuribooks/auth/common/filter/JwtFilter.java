package shop.nuribooks.auth.common.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.MemberRes;

public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtils jwtUtils;

	public JwtFilter(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String authorizationToken = request.getHeader("Authorization");
		if (authorizationToken == null || !authorizationToken.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String jwt = authorizationToken.split(" ")[1];

		// token 만료 처리 (현재는 access only)
		if (jwtUtils.isExpired(jwt)) {
			filterChain.doFilter(request, response);
			return;
		}

		// 유효한 경우 임시 세션 생성하여 등록
		String username = jwtUtils.getUsername(jwt);
		String role = jwtUtils.getRole(jwt);
		MemberRes member = new MemberRes();
		member.setUsername(username);
		member.setRole(role);

		CustomUserDetails userDetails = new CustomUserDetails(member);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}
}
