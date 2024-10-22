package shop.nuribooks.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import shop.nuribooks.auth.entity.RefreshToken;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {
	@Transactional
	void deleteByRefresh(String refresh);
	boolean existsByRefresh(String refresh);
}
