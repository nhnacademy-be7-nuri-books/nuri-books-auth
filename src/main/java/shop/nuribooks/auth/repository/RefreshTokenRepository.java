package shop.nuribooks.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import shop.nuribooks.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	boolean existsByRefresh(String refreshToken);

	RefreshToken findByRefresh(String refresh);

	@Transactional
	void deleteByRefresh(String refreshToken);
}
