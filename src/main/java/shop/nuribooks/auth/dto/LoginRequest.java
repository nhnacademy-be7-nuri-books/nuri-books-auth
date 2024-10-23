package shop.nuribooks.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// login 요청 시 JSON Data의 명세
@Getter
@NoArgsConstructor
public class LoginRequest {
	private String username;
	private String password;
}
