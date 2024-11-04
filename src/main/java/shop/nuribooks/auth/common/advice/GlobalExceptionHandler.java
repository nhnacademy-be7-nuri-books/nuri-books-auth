package shop.nuribooks.auth.common.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import shop.nuribooks.auth.common.exception.BadRequestException;
import shop.nuribooks.auth.common.exception.UnauthorizedException;
import shop.nuribooks.auth.common.exception.NotFoundException;
import shop.nuribooks.auth.common.message.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler({NotFoundException.class})
	public ResponseEntity<ErrorResponse> notFoundExceptionHandler(NotFoundException ex, WebRequest request) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	@ExceptionHandler({BadRequestException.class})
	public ResponseEntity<ErrorResponse> badRequestExceptionHandler(BadRequestException ex, WebRequest request) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	@ExceptionHandler({UnauthorizedException.class})
	public ResponseEntity<ErrorResponse> unauthorizedExceptionHandler(UnauthorizedException ex, WebRequest request) {
		return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
	}

	public ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus statusCode, String message, WebRequest request) {
		return new ResponseEntity<>(
			new ErrorResponse(
				statusCode.value(),
				message,
				request.getDescription(false)), statusCode);
	}
}
