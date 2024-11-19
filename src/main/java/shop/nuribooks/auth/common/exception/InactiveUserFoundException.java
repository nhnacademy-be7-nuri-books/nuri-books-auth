package shop.nuribooks.auth.common.exception;

public class InactiveUserFoundException extends RuntimeException {
	public InactiveUserFoundException(String message) {
		super(message);
	}
}
