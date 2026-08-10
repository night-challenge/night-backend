package com.nightchallenge.backend.global.exception;

import com.nightchallenge.backend.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 용도: Controller 예외 공통 처리
 * Controller에서 발생한 예외를 가로채 공통 ApiResponse 형식으로 변환한다.
 * 클라이언트가 기능과 관계없이 동일한 구조로 오류를 처리할 수 있게 한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 용도: 비즈니스 예외 처리
     * 서비스가 의도적으로 발생시킨 비즈니스 예외를 지정된 HTTP 상태와 메시지로 반환한다.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(exception.getMessage()));
    }

    /**
     * 용도: 요청값 검증 실패 처리
     * 요청 DTO의 필수값이나 형식 검증이 실패하면 첫 번째 검증 메시지를 400 응답으로 반환한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());

        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.error(message));
    }

    /**
     * 용도: 읽을 수 없는 요청 본문 처리
     * JSON 문법이나 값의 자료형이 잘못되어 요청 본문을 읽을 수 없는 경우 400 응답으로 반환한다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return createErrorResponse(ErrorCode.INVALID_REQUEST);
    }

    /**
     * 용도: 예상하지 못한 서버 오류 처리
     * 별도로 분류하지 못한 예외는 내부 내용을 노출하지 않고 공통 500 응답으로 반환한다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        return createErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 용도: 기본 오류 응답 생성
     * 기본 메시지를 사용하는 오류 응답 생성 과정을 한곳에 모아 중복을 줄인다.
     */
    private ResponseEntity<ApiResponse<Void>> createErrorResponse(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.error(errorCode.getMessage()));
    }
}
