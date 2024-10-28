package shop.nuribooks.auth.common.filter;

import java.io.IOException;

import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.util.CookieUtils;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {
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

		String refreshToken = CookieUtils.getValue(request, "Refresh");
		if (refreshToken == null || refreshToken.isBlank()) {
			log.info("Refresh Token 없는 유저의 로그아웃 요청입니다.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (!jwtUtils.getTokenType(refreshToken).equals("Refresh")) {
			log.info("Refresh Token이 아닙니다.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (jwtUtils.isExpired(refreshToken)) {
			log.info("이미 완료된 Refresh Token입니다.");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		if (!refreshTokenRepository.existsByRefreshToken(refreshToken)) {
			log.info("요청한 Refresh Token은 존재하지 않습니다.");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		refreshTokenRepository.deleteByRefreshToken(refreshToken);

		// TODO: Access Token의 생명주기가 짧아 front에서만 쿠키를 삭제해도 괜찮을 것 같은데, 아니면 Blacklist 도입할까?
		log.info("로그아웃 성공!");
		response.addCookie(CookieUtils.createCookie("Refresh", null, 0));
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
