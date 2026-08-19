package com.nightchallenge.backend.engravingrequest;

import com.nightchallenge.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: 각인 신청 오류 코드 테스트.
 * 선택값 누락 조합과 신청 건 미존재 오류가 명세의 HTTP 상태와 메시지를 제공하는지 검증한다.
 */
class EngravingRequestErrorCodeTest {

    @Test
    @DisplayName("각인 미선택 오류는 명세의 400 응답 정보를 가진다")
    void providesMissingEngravingError() {
        ErrorCode errorCode = ErrorCode.ENGRAVING_REQUEST_ENGRAVING_REQUIRED;

        assertThat(errorCode.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorCode.getMessage()).isEqualTo("제품에 새길 각인을 선택해 주세요.");
    }

    @Test
    @DisplayName("각인 색상 미선택 오류는 명세의 400 응답 정보를 가진다")
    void providesMissingColorError() {
        ErrorCode errorCode = ErrorCode.ENGRAVING_REQUEST_COLOR_REQUIRED;

        assertThat(errorCode.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorCode.getMessage()).isEqualTo("각인 색상을 선택해 주세요.");
    }

    @Test
    @DisplayName("각인과 색상 모두 미선택 오류는 조합 메시지를 가진다")
    void providesMissingSelectionsError() {
        ErrorCode errorCode = ErrorCode.ENGRAVING_REQUEST_SELECTION_REQUIRED;

        assertThat(errorCode.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(errorCode.getMessage())
                .isEqualTo("제품에 새길 각인과 각인 색상을 선택해 주세요.");
    }

    @Test
    @DisplayName("존재하지 않는 신청 건 오류는 명세의 404 응답 정보를 가진다")
    void providesEngravingRequestNotFoundError() {
        ErrorCode errorCode = ErrorCode.ENGRAVING_REQUEST_NOT_FOUND;

        assertThat(errorCode.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(errorCode.getMessage()).isEqualTo("존재하지 않는 신청 건입니다.");
    }
}
