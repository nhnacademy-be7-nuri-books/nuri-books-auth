package shop.nuribooks.auth.dto;

import lombok.Getter;
import lombok.Setter;

// TODO: 회원 가입을 위해 사용하는 임시 DTO로 추후 삭제 예정
@Getter
@Setter
public class MemberCreateReq {
	private String username;
	private String password;
}
