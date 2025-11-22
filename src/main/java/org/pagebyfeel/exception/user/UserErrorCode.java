package org.pagebyfeel.exception.user;

import lombok.Getter;

@Getter
public enum UserErrorCode {
    // 인증 관련
    TOKEN_EXPIRED("토큰이 만료되었습니다."),
    INVALID_TOKEN("유효하지 않은 토큰입니다."),
    TOKEN_NOT_FOUND("토큰을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED("리프레시 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN("유효하지 않은 리프레시 토큰입니다."),
    UNAUTHORIZED("인증되지 않은 사용자입니다."),
    ACCESS_DENIED("접근 권한이 없습니다."),
    
    // 사용자 조회/존재 관련
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS("이미 존재하는 사용자입니다."),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME("이미 사용 중인 닉네임입니다."),
    
    // OAuth 관련
    OAUTH_PROVIDER_NOT_SUPPORTED("지원하지 않는 OAuth 제공자입니다."),
    OAUTH_LOGIN_FAILED("OAuth 로그인에 실패했습니다."),
    OAUTH_TOKEN_REQUEST_FAILED("OAuth 토큰 요청에 실패했습니다."),
    OAUTH_USER_INFO_REQUEST_FAILED("OAuth 사용자 정보 요청에 실패했습니다."),
    
    // 비밀번호 관련 (나중에 자체 로그인 추가시)
    INVALID_PASSWORD("잘못된 비밀번호입니다."),
    PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다."),
    WEAK_PASSWORD("비밀번호 강도가 약합니다."),
    
    // 계정 상태 관련
    ACCOUNT_DISABLED("비활성화된 계정입니다."),
    ACCOUNT_LOCKED("잠긴 계정입니다."),
    ACCOUNT_DELETED("삭제된 계정입니다."),
    ACCOUNT_SUSPENDED("정지된 계정입니다."),
    
    // 권한 관련
    INSUFFICIENT_PERMISSIONS("권한이 부족합니다."),
    ADMIN_ONLY("관리자만 접근 가능합니다."),
    
    // 프로필 관련
    INVALID_PROFILE_IMAGE("유효하지 않은 프로필 이미지입니다."),
    PROFILE_IMAGE_TOO_LARGE("프로필 이미지 크기가 너무 큽니다."),
    INVALID_NICKNAME_FORMAT("닉네임 형식이 올바르지 않습니다."),
    NICKNAME_TOO_SHORT("닉네임이 너무 짧습니다."),
    NICKNAME_TOO_LONG("닉네임이 너무 깁니다."),
    
    // 검증 관련
    INVALID_EMAIL_FORMAT("이메일 형식이 올바르지 않습니다."),
    REQUIRED_FIELD_MISSING("필수 입력 항목이 누락되었습니다."),
    INVALID_USER_INPUT("유효하지 않은 입력값입니다."),
    
    // 서버 에러
    INTERNAL_SERVER_ERROR("서버 에러가 발생했습니다."),
    DATABASE_ERROR("데이터베이스 에러가 발생했습니다."),
    EXTERNAL_API_ERROR("외부 API 호출 중 에러가 발생했습니다.");

    private final String message;

    UserErrorCode(String message) {
        this.message = message;
    }
}
