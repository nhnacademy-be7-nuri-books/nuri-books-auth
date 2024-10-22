package shop.nuribooks.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

// TODO: Refresh entity로 field 확장 또는 redis로 연동하는 경우 수정 예정
@Getter
@Setter
@Entity
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;
	private String refresh;
	private String expiration;
}
