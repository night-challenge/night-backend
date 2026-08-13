package com.nightchallenge.backend.engravingrequest.repository;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationPoint;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.repository.NightPathRecordRepository;
import com.nightchallenge.backend.engravingrequest.domain.EngravingColor;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequest;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequestStatus;
import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import com.nightchallenge.backend.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 용도: 제품 각인 신청 Repository 동작 검증.
 * H2 테스트 DB에서 연관관계 저장, 사용자·상태별 조회와 제품 코드 중복 확인을 검증한다.
 */
@DataJpaTest
class EngravingRequestRepositoryTest {

    @Autowired
    private EngravingRequestRepository engravingRequestRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private NightPathRecordRepository nightPathRecordRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("제품 옵션과 보유 각인을 연결한 신청을 저장한다")
    void saveEngravingRequest() {
        ProductOption productOption = saveProductOption();
        NightPathRecord nightPathRecord = saveNightPathRecord(100L);

        EngravingRequest saved = engravingRequestRepository.saveAndFlush(new EngravingRequest(
                1L,
                productOption,
                nightPathRecord,
                EngravingColor.SILVER,
                "NWdfw25"
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProductOption().getId()).isEqualTo(productOption.getId());
        assertThat(saved.getNightPathRecord().getId()).isEqualTo(nightPathRecord.getId());
        assertThat(saved.getStatus()).isEqualTo(EngravingRequestStatus.COMPLETED);

        Object[] storedValues = (Object[]) entityManager.createNativeQuery(
                        "select engraving_color, status from engraving_requests where id = :id"
                )
                .setParameter("id", saved.getId())
                .getSingleResult();
        assertThat(storedValues[0]).isEqualTo("silver");
        assertThat(storedValues[1]).isEqualTo("신청완료");
    }

    @Test
    @DisplayName("사용자와 신청 상태로 필터링해 최신 신청부터 조회한다")
    void findByUserIdAndStatus() {
        ProductOption productOption = saveProductOption();
        NightPathRecord firstRecord = saveNightPathRecord(101L);
        NightPathRecord secondRecord = saveNightPathRecord(102L);
        EngravingRequest first = engravingRequestRepository.save(new EngravingRequest(
                1L, productOption, firstRecord, EngravingColor.GOLD, "Code001"
        ));
        EngravingRequest second = engravingRequestRepository.save(new EngravingRequest(
                1L, productOption, secondRecord, EngravingColor.BLACK, "Code002"
        ));
        NightPathRecord thirdRecord = saveNightPathRecord(104L);
        EngravingRequest third = engravingRequestRepository.save(new EngravingRequest(
                1L, productOption, thirdRecord, EngravingColor.SILVER, "Code003"
        ));
        ReflectionTestUtils.setField(second, "createdAt", LocalDateTime.of(2026, 8, 12, 10, 0));
        ReflectionTestUtils.setField(third, "createdAt", LocalDateTime.of(2026, 8, 13, 10, 0));
        first.cancel();
        engravingRequestRepository.flush();

        List<EngravingRequest> completed = engravingRequestRepository
                .findAllByUserIdAndStatusOrderByCreatedAtDesc(1L, EngravingRequestStatus.COMPLETED);
        List<EngravingRequest> canceled = engravingRequestRepository
                .findAllByUserIdAndStatusOrderByCreatedAtDesc(1L, EngravingRequestStatus.CANCELED);

        assertThat(completed).extracting(EngravingRequest::getId)
                .containsExactly(third.getId(), second.getId());
        assertThat(canceled).extracting(EngravingRequest::getId)
                .containsExactly(first.getId());
    }

    @Test
    @DisplayName("저장된 제품 코드의 중복 여부를 확인한다")
    void existsByProductCode() {
        ProductOption productOption = saveProductOption();
        NightPathRecord nightPathRecord = saveNightPathRecord(103L);
        engravingRequestRepository.saveAndFlush(new EngravingRequest(
                1L, productOption, nightPathRecord, EngravingColor.GOLD, "Ab12Cd3"
        ));

        assertThat(engravingRequestRepository.existsByProductCode("Ab12Cd3")).isTrue();
        assertThat(engravingRequestRepository.existsByProductCode("Xy98Za7")).isFalse();
    }

    @Test
    @DisplayName("동일한 제품 코드는 DB UNIQUE 제약으로 중복 저장할 수 없다")
    void productCodeMustBeUnique() {
        ProductOption productOption = saveProductOption();
        NightPathRecord firstRecord = saveNightPathRecord(105L);
        NightPathRecord secondRecord = saveNightPathRecord(106L);
        engravingRequestRepository.saveAndFlush(new EngravingRequest(
                1L, productOption, firstRecord, EngravingColor.GOLD, "Unique1"
        ));

        assertThatThrownBy(() -> engravingRequestRepository.saveAndFlush(new EngravingRequest(
                1L, productOption, secondRecord, EngravingColor.BLACK, "Unique1"
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    /**
     * 용도: 테스트용 제품 옵션 저장.
     * 신청 외래 키로 사용할 제품과 옵션을 함께 저장하고 옵션을 반환한다.
     */
    private ProductOption saveProductOption() {
        Product product = new Product(ProductCategory.BAG, "가방 설명");
        ProductOption option = new ProductOption("L Aren 비세토스 스쿨 토트", "갈색", 1_250_000);
        product.addOption(option);
        productRepository.saveAndFlush(product);
        return option;
    }

    /**
     * 용도: 테스트용 보유 각인 저장.
     * 신청 외래 키로 사용할 보유 각인을 게임 세션별로 생성해 저장한다.
     */
    private NightPathRecord saveNightPathRecord(Long gameSessionId) {
        ConstellationShape shape = new ConstellationShape(
                List.of(new ConstellationPoint(0, 1, 3)),
                List.of()
        );
        return nightPathRecordRepository.saveAndFlush(new NightPathRecord(
                1L,
                gameSessionId,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "플레이 분석 코멘트",
                new ConstellationData(shape, shape)
        ));
    }
}
