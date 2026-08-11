package com.nightchallenge.backend.engraving;

import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: 각인 오류 코드 테스트.
 * 각인 조회와 이름 수정 실패가 명세의 HTTP 상태 및 오류 메시지를 사용하는지 검증한다.
 */
class EngravingErrorCodeTest {

    @Test
    @DisplayName("존재하지 않는 각인은 404 오류 정보를 사용한다")
    void usesNotFoundErrorForMissingEngraving() {
        BusinessException exception = new BusinessException(ErrorCode.ENGRAVING_NOT_FOUND);

        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 각인입니다.");
    }

    @Test
    @DisplayName("기존 이름과 동일한 요청은 400 오류 정보를 사용한다")
    void usesBadRequestErrorForUnchangedName() {
        BusinessException exception = new BusinessException(ErrorCode.ENGRAVING_NAME_UNCHANGED);

        assertThat(exception.getErrorCode().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo("변경된 사항이 없습니다.");
    }
}
