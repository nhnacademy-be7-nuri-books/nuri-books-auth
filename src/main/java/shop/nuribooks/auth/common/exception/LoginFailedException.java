package shop.nuribooks.auth.common.exception;

public class LoginFailedException extends RuntimeException {
	public LoginFailedException(String message) {
		super(message);
	}
}
