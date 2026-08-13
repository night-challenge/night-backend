package com.nightchallenge.backend.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 용도: 공통 오류 정보 정의
 * 백엔드 전체에서 사용하는 HTTP 상태와 기본 오류 메시지를 한곳에서 관리한다.
 * 기능 코드가 상태 코드와 메시지를 직접 반복하지 않게 하여 오류 응답의 기준을 통일한다.
 */
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 데이터를 찾을 수 없습니다."),
    ENGRAVING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 각인입니다."),
    ENGRAVING_NAME_UNCHANGED(HttpStatus.BAD_REQUEST, "변경된 사항이 없습니다."),
    ENGRAVING_REQUEST_ENGRAVING_REQUIRED(HttpStatus.BAD_REQUEST, "제품에 새길 각인을 선택해 주세요."),
    ENGRAVING_REQUEST_COLOR_REQUIRED(HttpStatus.BAD_REQUEST, "각인 색상을 선택해 주세요."),
    ENGRAVING_REQUEST_SELECTION_REQUIRED(HttpStatus.BAD_REQUEST, "제품에 새길 각인과 각인 색상을 선택해 주세요."),
    ENGRAVING_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 신청 건입니다."),
    ENGRAVING_REQUEST_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 취소된 신청 건입니다."),
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게임입니다."),
    GAME_ALREADY_FINISHED(HttpStatus.BAD_REQUEST, "이미 종료된 게임입니다."),
    INVALID_MOVE(HttpStatus.BAD_REQUEST, "이동할 수 없는 위치입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    /**
     * 용도: 오류 코드 응답 정보 설정
     * 오류 종류마다 클라이언트에 반환할 HTTP 상태와 기본 메시지를 연결한다.
     */
    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    /**
     * 용도: HTTP 상태 반환
     * 전역 예외 처리기가 실제 HTTP 응답 상태를 결정할 때 사용한다.
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * 용도: 기본 오류 메시지 반환
     * 별도의 상세 메시지가 없는 오류에 공통 안내 문구를 제공한다.
     */
    public String getMessage() {
        return message;
    }
}
