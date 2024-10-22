package shop.nuribooks.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReq {
	private String username;
	private String password;
}
