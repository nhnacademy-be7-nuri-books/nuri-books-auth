package shop.nuribooks.auth.dto;

public record MemberResponse(String username, String password, String role, Long customerId) {
	/**
	 * String으로 status :
	 * 	STATUS_ACTIVE, INACTIVE(휴면) : 로그인 허용
	 * 	WITHDRAWN(탈퇴) : 로그인 불가
	 */
}
