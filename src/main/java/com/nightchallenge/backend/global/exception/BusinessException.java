package com.nightchallenge.backend.global.exception;

/**
 * 용도: 예상 가능한 비즈니스 오류 표현
 * 데이터 미존재처럼 서비스 흐름에서 예상할 수 있는 오류를 표현한다.
 * 기능별 Service가 이 예외를 발생시키면 전역 예외 처리기가 지정된 오류 응답으로 변환한다.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 용도: 기본 메시지 예외 생성
     * 공통 오류 코드의 기본 메시지를 사용하여 비즈니스 예외를 생성한다.
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 용도: 상세 메시지 예외 생성
     * 상황에 맞는 상세 메시지를 사용하되 HTTP 상태는 공통 오류 코드의 값을 유지한다.
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 용도: 예외 오류 코드 반환
     * 전역 예외 처리기가 예외에 연결된 HTTP 상태와 기본 정보를 확인할 때 사용한다.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
