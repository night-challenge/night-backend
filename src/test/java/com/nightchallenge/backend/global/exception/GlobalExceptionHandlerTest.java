package com.nightchallenge.backend.global.exception;

import com.nightchallenge.backend.global.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: 공통 예외 처리 결과 검증
 * 전역 예외 처리기가 오류 종류에 맞는 HTTP 상태와 공통 응답 본문을 만드는지 검증한다.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    /**
     * 용도: 404 비즈니스 오류 검증
     * 존재하지 않는 데이터를 나타내는 비즈니스 예외가 공통 404 응답으로 변환되는지 검증한다.
     */
    @Test
    void handlesNotFoundBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                "요청한 데이터를 찾을 수 없습니다."
        );
    }

    /**
     * 용도: 요청값 검증 오류 검증
     * 요청 DTO 검증 실패 시 구체적인 검증 메시지가 공통 400 응답에 포함되는지 검증한다.
     */
    @Test
    void handlesValidationException() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "name", "이름은 필수입니다."));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "이름은 필수입니다.");
    }

    /**
     * 용도: 잘못된 JSON 요청 검증
     * 읽을 수 없는 JSON 요청이 상세 내부 오류 없이 공통 400 응답으로 변환되는지 검증한다.
     */
    @Test
    void handlesUnreadableRequestBody() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "invalid json",
                new MockHttpInputMessage(new byte[0])
        );

        ResponseEntity<ApiResponse<Void>> response = handler.handleUnreadableMessage(exception);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "잘못된 요청입니다.");
    }

    /**
     * 용도: 예상하지 못한 서버 오류 검증
     * 예상하지 못한 예외가 상세 내용을 노출하지 않고 공통 500 응답으로 변환되는지 검증한다.
     */
    @Test
    void handlesUnexpectedException() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpectedException(
                new IllegalStateException("노출하면 안 되는 내부 오류")
        );

        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }

    /**
     * 용도: 공통 오류 응답 필드 검증
     * 모든 오류 테스트가 동일하게 확인하는 HTTP 상태와 ApiResponse 필드를 한 번에 검증한다.
     */
    private void assertErrorResponse(
            ResponseEntity<ApiResponse<Void>> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("error");
        assertThat(response.getBody().message()).isEqualTo(expectedMessage);
        assertThat(response.getBody().data()).isNull();
    }
}
