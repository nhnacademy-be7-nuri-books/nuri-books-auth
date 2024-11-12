package shop.nuribooks.auth.common.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.nuribooks.auth.common.filter.CustomLoginFilter;
import shop.nuribooks.auth.common.filter.CustomLogoutFilter;
import shop.nuribooks.auth.common.filter.JwtFilter;
import shop.nuribooks.auth.common.message.ErrorResponse;
import shop.nuribooks.auth.common.util.JwtUtils;
import shop.nuribooks.auth.repository.RefreshTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws
		Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, JwtUtils jwtUtils, RefreshTokenRepository refreshTokenRepository) throws Exception {
		http
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(new AuthenticationEntryPoint() {
					@Override
					public void commence(HttpServletRequest request, HttpServletResponse response,
						AuthenticationException authException) throws IOException, ServletException {
						ObjectMapper objectMapper = new ObjectMapper();
						ErrorResponse errorResponse = new ErrorResponse(
							HttpServletResponse.SC_UNAUTHORIZED,
							"Unauthorized access (" + authException.getLocalizedMessage() + ")",
							request.getRequestURI()
						);
						response.setContentType("application/json;charset=UTF-8");
						response.setStatus(errorResponse.statusCode());
						response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
					}
				}));

		http
			.cors(cors -> cors
				.configurationSource(new CorsConfigurationSource() {
					@Override
					public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
						CorsConfiguration configuration = new CorsConfiguration();
						// TODO: Front Server 이중화 시, Origins 추가
						configuration.setAllowedOrigins(Arrays.asList(
							"http://localhost:3000",
							"http://localhost:3001",
							"http://nuribooks.shop",
							"https://localhost:3000",
							"https://localhost:3001",
							"https://nuribooks.shop"
						));
						configuration.setAllowedMethods(Collections.singletonList("*"));
						configuration.setAllowCredentials(true);
						configuration.setAllowedHeaders(Collections.singletonList("*"));
						configuration.setExposedHeaders(Collections.singletonList("Authorization"));
						configuration.setMaxAge(60 * 60L);
						return configuration;
					}
				}));

		http
			.csrf(AbstractHttpConfigurer::disable);

		http.
			formLogin(AbstractHttpConfigurer::disable);

		http
			.httpBasic(AbstractHttpConfigurer::disable);

		http
			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll());

		http
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http
			.addFilterBefore(new CustomLogoutFilter(jwtUtils, refreshTokenRepository), LogoutFilter.class);

		http
			.addFilterBefore(new JwtFilter(jwtUtils), CustomLoginFilter.class);

		http
			.addFilterAt(new CustomLoginFilter(authenticationManager, jwtUtils, refreshTokenRepository), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers("/favicon.ico", "/static/**");
	}
}
