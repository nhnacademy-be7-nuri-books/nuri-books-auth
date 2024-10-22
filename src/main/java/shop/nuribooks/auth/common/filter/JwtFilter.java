package shop.nuribooks.auth.common.filter;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.dto.CustomUserDetails;
import shop.nuribooks.auth.dto.AuthorizedUser;

public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtils jwtUtils;

	public JwtFilter(JwtUtils jwtUtils) {
		this.jwtUtils = jwtUtils;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {
		String accessToken = request.getHeader("access");
		if (accessToken == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			jwtUtils.isExpired(accessToken);
		} catch (ExpiredJwtException ex) {
			PrintWriter out = response.getWriter();
			out.println("Access Token is EXPIRED");
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			return;
		}

		String tokenCategory = jwtUtils.getCategory(accessToken);
		if (!tokenCategory.equals("access")) {
			PrintWriter out = response.getWriter();
			out.println("Access Token is INVALID");
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			return;
		}

		String username = jwtUtils.getUsername(accessToken);
		String role = jwtUtils.getRole(accessToken);
		AuthorizedUser user = new AuthorizedUser();
		user.setUsername(username);
		user.setRole(role);

		CustomUserDetails userDetails = new CustomUserDetails(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}
}
