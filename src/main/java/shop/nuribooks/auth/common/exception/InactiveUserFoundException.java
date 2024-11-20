package shop.nuribooks.auth.common.exception;

import org.springframework.security.core.AuthenticationException;

public class InactiveUserFoundException extends AuthenticationException {
    public InactiveUserFoundException(String message) {
        super(message);
    }
}
