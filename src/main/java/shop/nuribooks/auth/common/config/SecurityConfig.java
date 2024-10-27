package shop.nuribooks.auth.common.config;

import java.io.IOException;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shop.nuribooks.auth.common.filter.CustomLoginFilter;
import shop.nuribooks.auth.common.filter.JwtFilter;
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
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					}
				}));

		http
			.cors(cors -> cors
				.configurationSource(new CorsConfigurationSource() {
					@Override
					public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
						CorsConfiguration configuration = new CorsConfiguration();
						configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));	// client가 접근하는 URL
						configuration.setAllowedMethods(Collections.singletonList("*"));
						configuration.setAllowCredentials(true);	// cookie 포함
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
				.requestMatchers("/", "/login", "/reissue").permitAll()
				.requestMatchers("/admin").hasRole("ADMIN")
				.anyRequest().authenticated());

		http
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http
			.addFilterBefore(new JwtFilter(jwtUtils), CustomLoginFilter.class);

		http
			.addFilterAt(new CustomLoginFilter(authenticationManager, jwtUtils, refreshTokenRepository), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails admin = User.builder()
			.username("admin")
			.password(bCryptPasswordEncoder().encode("123"))
			.roles("ADMIN")
			.build();

		UserDetails user = User.builder()
			.username("user")
			.password(bCryptPasswordEncoder().encode("123"))
			.roles("USER")
			.build();

		return new InMemoryUserDetailsManager(admin, user);
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().requestMatchers("/favicon.ico", "/static/**");
	}
}
