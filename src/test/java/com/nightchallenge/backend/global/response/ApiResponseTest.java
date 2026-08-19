package com.nightchallenge.backend.global.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void createsSuccessfulResponseForQuery() {
        // 조회 성공 응답이 실제 data를 포함하고 별도의 안내 문구는 사용하지 않는지 검증한다.
        ApiResponse<String> response = ApiResponse.success("result");

        assertThat(response.status()).isEqualTo("success");
        assertThat(response.message()).isNull();
        assertThat(response.data()).isEqualTo("result");
    }

    @Test
    void createsSuccessfulResponseForStateChange() {
        // 수정과 같은 상태 변경 응답이 성공 안내 문구와 결과 데이터를 함께 전달하는지 검증한다.
        ApiResponse<Long> response = ApiResponse.success("수정되었습니다.", 1L);

        assertThat(response.status()).isEqualTo("success");
        assertThat(response.message()).isEqualTo("수정되었습니다.");
        assertThat(response.data()).isEqualTo(1L);
    }

    @Test
    void createsErrorResponse() {
        // 실패 응답이 오류 문구를 포함하고 실제 data는 null로 반환하는지 검증한다.
        ApiResponse<Void> response = ApiResponse.error("존재하지 않는 제품입니다.");

        assertThat(response.status()).isEqualTo("error");
        assertThat(response.message()).isEqualTo("존재하지 않는 제품입니다.");
        assertThat(response.data()).isNull();
    }
}
