package shop.nuribooks.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRes {
	private String username;
	private String password;
	private String role;
}
