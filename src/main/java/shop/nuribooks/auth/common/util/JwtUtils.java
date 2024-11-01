package shop.nuribooks.auth.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtUtils {
	private final SecretKey secretKey;
	private final JwtProperties jwtProperties;

	public JwtUtils(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = new SecretKeySpec(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	public String getTokenType(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("tokenType", String.class);
		} catch (Exception ex) {
			log.info("TokenType을 가져오는데 실패하였습니다.");
		}
		return null;
	}

	public String getUsername(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
		} catch (Exception ex) {
			log.info("Username을 가져오는데 실패하였습니다.");
		}
		return null;
	}

	public String getRole(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
		} catch (Exception ex) {
			log.info("Role을 가져오는데 실패하였습니다.");
		}
		return null;
	}

	public Boolean isExpired(String token) {
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
		} catch (Exception ex) {
			log.info("만료기한을 가져오는데 실패하였습니다.");
		}
		return true;
	}

	public String createJwt(String tokenType, String username, String role, Long expiredMs) {
		return Jwts.builder()
			.claim("tokenType", tokenType)
			.claim("username", username)
			.claim("role", role)
			.issuedAt(new Date(System.currentTimeMillis()))
			.expiration(new Date(System.currentTimeMillis() + expiredMs))
			.claim("issuer", jwtProperties.getIssuer())
			.signWith(secretKey)
			.compact();
	}
}
