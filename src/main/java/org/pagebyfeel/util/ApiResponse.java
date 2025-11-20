package org.pagebyfeel.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;   // 요청 성공 여부
    private String message;    // 상태 메시지 (optional)
    private T data;            // 실제 데이터 (generic)
    private int code;          // 상태 코드 (HTTP status or custom code)

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", data, 200);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, 200);
    }

    public static <T> ApiResponse<T> fail(String message, int code) {
        return new ApiResponse<>(false, message, null, code);
    }
}

