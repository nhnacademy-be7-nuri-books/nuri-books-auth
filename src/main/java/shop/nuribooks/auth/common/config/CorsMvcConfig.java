package shop.nuribooks.auth.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins(
				"http://localhost:3000",
				"http://localhost:3001",
				"http://nuribooks.shop",
				"https://localhost:3000",
				"https://localhost:3001",
				"https://nuribooks.shop"
			)
			.allowCredentials(true)
			.allowedHeaders("*")
			.allowedMethods("*")
			.exposedHeaders("Authorization")
			.maxAge(60 * 60L);
	}
}