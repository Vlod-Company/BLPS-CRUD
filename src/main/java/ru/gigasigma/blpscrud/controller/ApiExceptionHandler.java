package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ApiFieldViolation> violations = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toViolation)
                .collect(Collectors.toList());

        return badRequest(
                "Validation error",
                "Request body contains invalid fields",
                request.getRequestURI(),
                violations
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {
        return badRequest(
                "Validation error",
                "Request parameters are invalid",
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ApiFieldViolation> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> new ApiFieldViolation(v.getPropertyPath().toString(), v.getMessage(), stringify(v.getInvalidValue())))
                .collect(Collectors.toList());

        return badRequest(
                "Validation error",
                "Request parameters are invalid",
                request.getRequestURI(),
                violations
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return badRequest(
                "Invalid JSON",
                "Request body is malformed or contains invalid value types",
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        return badRequest(
                "Missing required parameter",
                "Parameter '" + ex.getParameterName() + "' is required",
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        return badRequest(
                "Invalid parameter format",
                "Parameter '" + ex.getName() + "' has invalid format",
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        ));
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiErrorResponse> handleRouteNotFound(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Route not found",
                "No endpoint mapped for requested path",
                request.getRequestURI(),
                List.of()
        ));
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        return badRequest(
                "Operation cannot be completed",
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "Unexpected error occurred. Please try again later",
                request.getRequestURI(),
                List.of()
        ));
    }

    private ResponseEntity<ApiErrorResponse> badRequest(
            String error,
            String message,
            String path,
            List<ApiFieldViolation> violations
    ) {
        return ResponseEntity.badRequest().body(ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                error,
                message,
                path,
                violations
        ));
    }

    private ApiFieldViolation toViolation(FieldError fieldError) {
        return new ApiFieldViolation(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                stringify(fieldError.getRejectedValue())
        );
    }

    private String stringify(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @Schema(name = "ApiErrorResponse", description = "Unified API error payload")
    public record ApiErrorResponse(
            @Schema(description = "Time when the error response was generated", example = "2026-03-21T15:10:00")
            LocalDateTime timestamp,
            @Schema(description = "HTTP status code", example = "400")
            int status,
            @Schema(description = "Short error title", example = "Validation error")
            String error,
            @Schema(description = "Human-readable error details", example = "Request body contains invalid fields")
            String message,
            @Schema(description = "Request path that produced the error", example = "/api/orders")
            String path,
            @Schema(description = "List of invalid fields or parameters")
            List<ApiFieldViolation> violations
    ) {
        static ApiErrorResponse of(int status, String error, String message, String path, List<ApiFieldViolation> violations) {
            return new ApiErrorResponse(LocalDateTime.now(), status, error, message, path, violations);
        }
    }

    @Schema(name = "ApiFieldViolation", description = "Validation error details for a single field")
    public record ApiFieldViolation(
            @Schema(description = "Field or parameter name", example = "userId")
            String field,
            @Schema(description = "Validation message", example = "userId must be a positive number")
            String message,
            @Schema(description = "Rejected value", example = "0")
            String rejectedValue
    ) {
    }
}
