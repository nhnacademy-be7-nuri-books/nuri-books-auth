package shop.nuribooks.auth.dto;

import lombok.Getter;
import lombok.Setter;

// UserDetails가 Wrapping할 유저 정보입니다.
@Getter
@Setter
public class AuthorizedUser {
	private String username;
	private String password;
	private String role;
}
