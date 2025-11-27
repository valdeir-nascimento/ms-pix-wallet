package br.com.pix.wallet.presentation.rest.error;

import br.com.pix.wallet.domain.exception.DomainException;
import br.com.pix.wallet.domain.exception.NotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import br.com.pix.wallet.domain.validation.Error;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(final DomainException ex) {
        return ResponseEntity.unprocessableEntity().body(ApiError.from(ex));
    }

    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(final NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.from(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        final List<Error> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(GlobalExceptionHandler::toDomainError)
            .toList();
        final ApiError apiError = new ApiError("Validation Error", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(OptimisticLockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiError("Concurrency error, try again",
                List.of(Error.of(ex.getMessage()))));
    }

    private static Error toDomainError(FieldError fieldError) {
        final String message = String.format("'%s' %s",
            fieldError.getField(),
            fieldError.getDefaultMessage()
        );
        return new Error(message);
    }


}
