package shop.nuribooks.auth.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			// TODO: Front Server 이중화 시, Origins 추가
			.allowedOrigins("http://localhost:3000")
			.allowCredentials(true)
			.allowedHeaders("*")
			.allowedMethods("*")
			.exposedHeaders("Authorization")
			.maxAge(60 * 60L);
	}
}
