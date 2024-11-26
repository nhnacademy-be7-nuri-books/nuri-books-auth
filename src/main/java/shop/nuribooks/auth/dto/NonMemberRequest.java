package shop.nuribooks.auth.dto;

public record NonMemberRequest(
	String email,
	String password
) {
}
