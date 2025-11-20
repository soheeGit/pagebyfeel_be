package org.pagebyfeel.exception;

import org.pagebyfeel.util.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ApiResponse<Object> handleCustomException(CustomException ex) {
        // CustomException 내부에 ErrorCode가 있으면 거기서 메시지와 코드 사용
        return ApiResponse.fail(
                ex.getErrorCode().getMessage(),
                HttpStatus.BAD_REQUEST.value() // 혹은 ErrorCode에서 커스텀 코드 가져오도록 확장 가능
        );
    }

    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ex.getMessage());

        return ApiResponse.fail(message, HttpStatus.BAD_REQUEST.value());
    }

    // 일반 서버 예외 처리
    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception ex) {
        ex.printStackTrace();
        return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
