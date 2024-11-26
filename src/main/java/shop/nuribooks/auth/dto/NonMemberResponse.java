package shop.nuribooks.auth.dto;

public record NonMemberResponse(
	Long customerId,
	String email,
	String password
) {
}
