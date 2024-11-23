package shop.nuribooks.auth.common.filter;

import java.io.IOException;

import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.message.ErrorResponse;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {
	private static final String REFRESH_TOKEN = "Refresh";
	private final JwtUtils jwtUtils;
	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws
		IOException,
		ServletException {
		doFilter((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse, filterChain);
	}

	private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws
		ServletException,
		IOException {
		if (!request.getRequestURI().matches("^/api/auth/logout$") || !request.getMethod().equals("POST")) {
			filterChain.doFilter(request, response);
			return;
		}

		String refreshToken = request.getHeader(REFRESH_TOKEN);
		if (refreshToken == null || refreshToken.isBlank()) {
			sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Refresh Token is NULL or Empty.");
			return;
		}

		if (!jwtUtils.getTokenType(refreshToken).equals(REFRESH_TOKEN)) {
			sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Refresh Token is Invalid.");
			return;
		}

		if (jwtUtils.isExpired(refreshToken)) {
			sendError(request, response, HttpServletResponse.SC_BAD_REQUEST, "Refresh Token is Expired.");
			return;
		}

		if (!refreshTokenRepository.existsByRefresh(refreshToken)) {
			sendError(request, response, HttpServletResponse.SC_NOT_FOUND, "Refresh Token does not Exist.");
			return;
		}

		refreshTokenRepository.deleteByRefresh(refreshToken);

		log.info("로그아웃 성공!");
		response.addCookie(CookieUtils.createCookie(REFRESH_TOKEN, null, 0));
		response.setStatus(HttpServletResponse.SC_OK);
	}

	private void sendError(HttpServletRequest request, HttpServletResponse response, int statusCode, String message) {
		response.setStatus(statusCode);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		try {
			String json = new ObjectMapper().writeValueAsString(
				new ErrorResponse(statusCode, message, request.getRequestURI()));
			response.getWriter().write(json);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
