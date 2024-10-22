package shop.nuribooks.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCreateReq {
	private String username;
	private String password;
}
