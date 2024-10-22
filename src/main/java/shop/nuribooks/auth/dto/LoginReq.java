package shop.nuribooks.auth.dto;

import lombok.Getter;
import lombok.Setter;

// login 요청 시 JSON Data의 명세
@Getter
@Setter
public class LoginReq {
	private String username;
	private String password;
}
