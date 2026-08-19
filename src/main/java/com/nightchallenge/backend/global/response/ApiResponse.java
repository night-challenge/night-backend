package com.nightchallenge.backend.global.response;

/**
 * 모든 API 응답을 status, message, data 형식으로 통일하는 공통 응답 객체다.
 * Controller가 실제 응답 데이터를 이 객체로 감싸 반환하면 프론트엔드는 모든 API를 동일한 구조로 처리할 수 있다.
 *
 * @param status  요청 성공 여부를 나타내는 값(success 또는 error)
 * @param message 성공 안내나 오류 원인을 전달하는 문구. 조회 성공 시에는 보통 null이다.
 * @param data    프론트엔드에 전달할 실제 응답 데이터. 전달할 데이터가 없으면 null이다.
 * @param <T>     제품, 각인, 마이페이지 등 API마다 달라지는 data의 자료형
 */

public record ApiResponse<T>(String status, String message, T data) {

    // API마다 상태 문자열을 직접 작성할 때 생길 수 있는 오타와 중복을 막기 위해 상수로 관리한다.
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";

    /**
     * GET 조회 API처럼 별도의 안내 문구 없이 실제 데이터만 반환할 때 사용한다.
     * status는 success, message는 null, data는 전달받은 값으로 구성한다.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(SUCCESS, null, data);
    }

    /**
     * 생성이나 수정 API처럼 처리 결과를 설명하는 성공 문구와 데이터를 함께 반환할 때 사용한다.
     * 예를 들어 각인 이름 수정 성공 시 "수정되었습니다."라는 문구와 수정 결과를 전달한다.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(SUCCESS, message, data);
    }

    /**
     * 잘못된 요청이나 존재하지 않는 자원처럼 요청 실패 이유를 반환할 때 사용한다.
     * status는 error, message는 오류 원인, data는 null로 구성하며 이후 전역 예외 처리에서도 재사용한다.
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(ERROR, message, null);
    }
}
