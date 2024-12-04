package shop.nuribooks.auth.common.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import shop.nuribooks.auth.common.exception.UnauthorizedException;

@Slf4j
@Component
public class JwtUtils {
	public static final Long ACCESS_TOKEN_VALID_TIME = 60 * 60 * 250L;
	public static final Long REFRESH_TOKEN_VALID_TIME = 60 * 60 * 1000L * 5;
	private final SecretKey secretKey;
	private final JwtProperties jwtProperties;

	public JwtUtils(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = new SecretKeySpec(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	public String getTokenType(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("tokenType", String.class);
		} catch (Exception ex) {
			throw new UnauthorizedException("토큰을 읽는데 실패하였습니다 : Token Type");
		}
	}

	public String getUsername(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("userId", String.class);
		} catch (Exception ex) {
			throw new UnauthorizedException("토큰을 읽는데 실패하였습니다 : Username");

		}
	}

	public String getRole(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.get("role", String.class);
		} catch (Exception ex) {
			throw new UnauthorizedException("토큰을 읽는데 실패하였습니다 : Role");
		}
	}

	public Boolean isExpired(String token) {
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getExpiration()
				.toInstant()
				.isBefore(Instant.now());
		} catch (Exception ex) {
			log.info("만료기한을 가져오는데 실패하였습니다.");
		}
		return true;
	}

	public String createJwt(String tokenType, String userId, String role, Long expiredMs) {
		Instant now = Instant.now();
		return Jwts.builder()
			.claim("tokenType", tokenType)
			.claim("userId", userId)
			.claim("role", role)
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusMillis(expiredMs)))
			.claim("issuer", jwtProperties.getIssuer())
			.signWith(secretKey)
			.compact();
	}
}
