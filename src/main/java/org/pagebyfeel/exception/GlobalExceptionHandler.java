package org.pagebyfeel.exception;

import lombok.extern.slf4j.Slf4j;
import org.pagebyfeel.exception.auth.AuthErrorCode;
import org.pagebyfeel.exception.common.BusinessException;
import org.pagebyfeel.exception.common.ErrorCode;
import org.pagebyfeel.exception.common.ErrorResponse;
import org.pagebyfeel.exception.global.GlobalErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * BusinessException 처리
     * 비즈니스 로직에서 발생하는 모든 커스텀 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("BusinessException occurred: {}", ex.getMessage());
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    /**
     * @Valid, @Validated 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("Validation error occurred: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

        ErrorResponse response = ErrorResponse.of(
                GlobalErrorCode.INVALID_INPUT_VALUE,
                errorMessage
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * @ModelAttribute 바인딩 실패 처리
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
        log.warn("Bind error occurred: {}", ex.getMessage());
        
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());

        ErrorResponse response = ErrorResponse.of(
                GlobalErrorCode.INVALID_INPUT_VALUE,
                errorMessage
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 타입 불일치 예외 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch error occurred: {}", ex.getMessage());
        
        String errorMessage = String.format("'%s' 파라미터의 타입이 올바르지 않습니다.", ex.getName());
        ErrorResponse response = ErrorResponse.of(
                GlobalErrorCode.INVALID_INPUT_VALUE,
                errorMessage
        );
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 지원하지 않는 HTTP 메서드 호출 시 처리
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported error occurred: {}", ex.getMessage());
        
        ErrorResponse response = ErrorResponse.of(GlobalErrorCode.METHOD_NOT_ALLOWED);
        
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(response);
    }

    /**
     * Spring Security - 인증 실패 예외 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error occurred: {}", ex.getMessage());
        
        throw new BusinessException(AuthErrorCode.AUTHENTICATION_FAILED);
    }

    /**
     * Spring Security - 권한 부족 예외 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied error occurred: {}", ex.getMessage());
        
        throw new BusinessException(AuthErrorCode.ACCESS_DENIED);
    }

    /**
     * HTTP Media Type Not Acceptable 예외 처리
     * 클라이언트가 요청한 Content-Type을 서버가 제공할 수 없을 때 발생
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptableException(
            HttpMediaTypeNotAcceptableException ex) {
        log.warn("Media type not acceptable error occurred: {}", ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
                "MEDIA_TYPE_NOT_ACCEPTABLE",
                "요청한 미디어 타입으로 응답을 제공할 수 없습니다. application/json을 사용해주세요.",
                HttpStatus.NOT_ACCEPTABLE.value(),
                java.time.LocalDateTime.now()
        );
        
        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(response);
    }

    /**
     * 정적 리소스를 찾을 수 없을 때 발생하는 예외 처리
     * 브라우저가 자동으로 요청하는 리소스(favicon, .well-known 등)에 대한 404를 조용히 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.debug("Static resource not found: {}", ex.getResourcePath());
        
        ErrorResponse response = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                "요청한 리소스를 찾을 수 없습니다.",
                HttpStatus.NOT_FOUND.value(),
                java.time.LocalDateTime.now()
        );
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * 모든 예상치 못한 예외 처리
     * 마지막 방어선으로 작동
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse response = ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR);
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
