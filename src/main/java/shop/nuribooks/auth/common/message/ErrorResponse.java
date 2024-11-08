package shop.nuribooks.auth.common.message;

public record ErrorResponse(int statusCode, String message, String details) {
}
