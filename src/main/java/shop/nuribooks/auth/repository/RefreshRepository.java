package shop.nuribooks.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import shop.nuribooks.auth.entity.RefreshToken;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {
	boolean existsByRefresh(String refresh);
	void deleteByRefresh(String refresh);
}
