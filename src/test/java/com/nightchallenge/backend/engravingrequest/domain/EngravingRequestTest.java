package com.nightchallenge.backend.engravingrequest.domain;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationPoint;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: 제품 각인 신청 Entity 동작 검증.
 * 신청 생성 시 연관 데이터와 기본 상태가 설정되고 취소 시 상태만 변경되는지 검증한다.
 */
class EngravingRequestTest {

    @Test
    @DisplayName("신청을 생성하면 제품 옵션·각인·색상과 완료 상태를 설정한다")
    void createEngravingRequest() {
        ProductOption productOption = createProductOption();
        NightPathRecord nightPathRecord = createNightPathRecord();

        EngravingRequest request = new EngravingRequest(
                1L,
                productOption,
                nightPathRecord,
                EngravingColor.GOLD,
                "NWdfw25"
        );

        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getProductOption()).isSameAs(productOption);
        assertThat(request.getNightPathRecord()).isSameAs(nightPathRecord);
        assertThat(request.getEngravingColor()).isEqualTo(EngravingColor.GOLD);
        assertThat(request.getProductCode()).isEqualTo("NWdfw25");
        assertThat(request.getStatus()).isEqualTo(EngravingRequestStatus.COMPLETED);
        assertThat(request.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("신청을 취소하면 데이터와 제품 코드는 유지하고 상태만 변경한다")
    void cancelEngravingRequest() {
        ProductOption productOption = createProductOption();
        NightPathRecord nightPathRecord = createNightPathRecord();
        EngravingRequest request = new EngravingRequest(
                1L,
                productOption,
                nightPathRecord,
                EngravingColor.BLACK,
                "Ab12Cd3"
        );

        request.cancel();

        assertThat(request.getStatus()).isEqualTo(EngravingRequestStatus.CANCELED);
        assertThat(request.getProductCode()).isEqualTo("Ab12Cd3");
        assertThat(request.getProductOption()).isSameAs(productOption);
        assertThat(request.getNightPathRecord()).isSameAs(nightPathRecord);
    }

    /**
     * 용도: 테스트용 제품 옵션 생성.
     * 신청 Entity 연관관계를 검증할 제품과 옵션을 연결해 반환한다.
     */
    private ProductOption createProductOption() {
        Product product = new Product(ProductCategory.BAG, "가방 설명");
        ProductOption option = new ProductOption("L Aren 비세토스 스쿨 토트", "갈색", 1_250_000);
        product.addOption(option);
        return option;
    }

    /**
     * 용도: 테스트용 보유 각인 생성.
     * 신청 Entity 연관관계를 검증할 before·after 별자리 데이터를 생성한다.
     */
    private NightPathRecord createNightPathRecord() {
        ConstellationShape shape = new ConstellationShape(
                List.of(new ConstellationPoint(0, 1, 3)),
                List.of()
        );
        return new NightPathRecord(
                1L,
                100L,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "플레이 분석 코멘트",
                new ConstellationData(shape, shape)
        );
    }
}
