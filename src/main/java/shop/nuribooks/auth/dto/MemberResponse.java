package shop.nuribooks.auth.dto;

public record MemberResponse(String username, String password, String role, Long customerId) {
}
