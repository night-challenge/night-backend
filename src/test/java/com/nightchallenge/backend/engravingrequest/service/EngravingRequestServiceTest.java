package com.nightchallenge.backend.engravingrequest.service;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationPoint;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.repository.NightPathRecordRepository;
import com.nightchallenge.backend.engravingrequest.domain.EngravingColor;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequest;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequestStatus;
import com.nightchallenge.backend.engravingrequest.dto.request.EngravingRequestCreateRequest;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestCreateResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestListResponse;
import com.nightchallenge.backend.engravingrequest.repository.EngravingRequestRepository;
import com.nightchallenge.backend.engravingrequest.support.ProductCodeGenerator;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import com.nightchallenge.backend.product.repository.ProductOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 용도: 제품 각인 신청 Service 단위 검증.
 * 신청 생성, 상태별 목록 조회, 취소와 오류 처리 흐름을 Repository 대역으로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class EngravingRequestServiceTest {

    @Mock
    private EngravingRequestRepository engravingRequestRepository;

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private NightPathRecordRepository nightPathRecordRepository;

    @Mock
    private ProductCodeGenerator productCodeGenerator;

    private EngravingRequestService engravingRequestService;

    @BeforeEach
    void setUp() {
        engravingRequestService = new EngravingRequestService(
                engravingRequestRepository,
                productOptionRepository,
                nightPathRecordRepository,
                productCodeGenerator
        );
    }

    @Test
    @DisplayName("제품 옵션과 보유 각인을 연결하고 고유 제품 코드로 신청을 생성한다")
    void createEngravingRequest() {
        ProductOption productOption = createProductOption(3L);
        NightPathRecord engraving = createNightPathRecord(1L, 1L);
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(
                1L, 3L, EngravingColor.GOLD
        );
        when(productOptionRepository.findById(3L)).thenReturn(Optional.of(productOption));
        when(nightPathRecordRepository.findById(1L)).thenReturn(Optional.of(engraving));
        when(productCodeGenerator.generateUniqueCode(any())).thenReturn("NWdfw25");
        when(engravingRequestRepository.save(any(EngravingRequest.class)))
                .thenAnswer(invocation -> {
                    EngravingRequest saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 5L);
                    return saved;
                });

        EngravingRequestCreateResponse response = engravingRequestService.createEngravingRequest(request);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.productCode()).isEqualTo("NWdfw25");
        verify(productCodeGenerator).generateUniqueCode(any());
        verify(engravingRequestRepository).save(any(EngravingRequest.class));
    }

    @Test
    @DisplayName("제품 코드 생성기는 Repository 중복 확인을 사용해 재시도한다")
    void createEngravingRequestWithDuplicateCodeRetry() {
        ProductOption productOption = createProductOption(3L);
        NightPathRecord engraving = createNightPathRecord(1L, 1L);
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(
                1L, 3L, EngravingColor.SILVER
        );
        when(productOptionRepository.findById(3L)).thenReturn(Optional.of(productOption));
        when(nightPathRecordRepository.findById(1L)).thenReturn(Optional.of(engraving));
        when(productCodeGenerator.generateUniqueCode(any())).thenAnswer(invocation -> {
            java.util.function.Predicate<String> checker = invocation.getArgument(0);
            assertThat(checker.test("Dup0001")).isTrue();
            assertThat(checker.test("New0001")).isFalse();
            return "New0001";
        });
        when(engravingRequestRepository.existsByProductCode("Dup0001")).thenReturn(true);
        when(engravingRequestRepository.existsByProductCode("New0001")).thenReturn(false);
        when(engravingRequestRepository.save(any(EngravingRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EngravingRequestCreateResponse response = engravingRequestService.createEngravingRequest(request);

        assertThat(response.productCode()).isEqualTo("New0001");
        verify(engravingRequestRepository).existsByProductCode("Dup0001");
        verify(engravingRequestRepository).existsByProductCode("New0001");
    }

    @Test
    @DisplayName("각인과 색상을 모두 선택하지 않으면 조합 오류를 반환한다")
    void createWithoutEngravingAndColor() {
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(null, 3L, null);

        assertBusinessException(
                () -> engravingRequestService.createEngravingRequest(request),
                ErrorCode.ENGRAVING_REQUEST_SELECTION_REQUIRED
        );
        verify(engravingRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("각인을 선택하지 않으면 각인 선택 오류를 반환한다")
    void createWithoutEngraving() {
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(
                null, 3L, EngravingColor.GOLD
        );

        assertBusinessException(
                () -> engravingRequestService.createEngravingRequest(request),
                ErrorCode.ENGRAVING_REQUEST_ENGRAVING_REQUIRED
        );
    }

    @Test
    @DisplayName("색상을 선택하지 않으면 색상 선택 오류를 반환한다")
    void createWithoutColor() {
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(1L, 3L, null);

        assertBusinessException(
                () -> engravingRequestService.createEngravingRequest(request),
                ErrorCode.ENGRAVING_REQUEST_COLOR_REQUIRED
        );
    }

    @Test
    @DisplayName("존재하지 않는 제품 옵션이면 404 오류를 반환한다")
    void createWithMissingProductOption() {
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(
                1L, 99L, EngravingColor.BLACK
        );
        when(productOptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> engravingRequestService.createEngravingRequest(request))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("존재하지 않는 제품입니다.");
                });
    }

    @Test
    @DisplayName("존재하지 않거나 다른 사용자의 각인이면 404 오류를 반환한다")
    void createWithMissingEngraving() {
        ProductOption productOption = createProductOption(3L);
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(
                99L, 3L, EngravingColor.BLACK
        );
        when(productOptionRepository.findById(3L)).thenReturn(Optional.of(productOption));
        when(nightPathRecordRepository.findById(99L)).thenReturn(Optional.empty());

        assertBusinessException(
                () -> engravingRequestService.createEngravingRequest(request),
                ErrorCode.ENGRAVING_NOT_FOUND
        );
    }

    @Test
    @DisplayName("상태별 신청 목록에는 제품 정보와 최종 After만 포함한다")
    void getEngravingRequests() {
        EngravingRequest engravingRequest = createEngravingRequest(5L, EngravingRequestStatus.COMPLETED);
        when(engravingRequestRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(
                1L, EngravingRequestStatus.COMPLETED
        )).thenReturn(List.of(engravingRequest));

        EngravingRequestListResponse response = engravingRequestService
                .getEngravingRequests(EngravingRequestStatus.COMPLETED);

        assertThat(response.records()).hasSize(1);
        assertThat(response.records().get(0).product().optionName())
                .isEqualTo("L Aren 비세토스 스쿨 토트");
        assertThat(response.records().get(0).engraving().constellationData().points())
                .extracting(point -> point.id())
                .containsExactly(10);
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 records 배열을 반환한다")
    void getEmptyEngravingRequests() {
        when(engravingRequestRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(
                1L, EngravingRequestStatus.CANCELED
        )).thenReturn(List.of());

        EngravingRequestListResponse response = engravingRequestService
                .getEngravingRequests(EngravingRequestStatus.CANCELED);

        assertThat(response.records()).isEmpty();
    }

    @Test
    @DisplayName("신청을 취소하면 삭제하지 않고 상태만 취소됨으로 변경한다")
    void cancelEngravingRequest() {
        EngravingRequest engravingRequest = createEngravingRequest(5L, EngravingRequestStatus.COMPLETED);
        when(engravingRequestRepository.findById(5L)).thenReturn(Optional.of(engravingRequest));

        engravingRequestService.cancelEngravingRequest(5L);

        assertThat(engravingRequest.getStatus()).isEqualTo(EngravingRequestStatus.CANCELED);
        verify(engravingRequestRepository, never()).delete(any());
    }

    @Test
    @DisplayName("존재하지 않는 신청을 취소하면 404 오류를 반환한다")
    void cancelMissingEngravingRequest() {
        when(engravingRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertBusinessException(
                () -> engravingRequestService.cancelEngravingRequest(99L),
                ErrorCode.ENGRAVING_REQUEST_NOT_FOUND
        );
    }

    @Test
    @DisplayName("이미 취소된 신청을 다시 취소하면 400 오류를 반환한다")
    void cancelAlreadyCanceledEngravingRequest() {
        EngravingRequest engravingRequest = createEngravingRequest(5L, EngravingRequestStatus.CANCELED);
        when(engravingRequestRepository.findById(5L)).thenReturn(Optional.of(engravingRequest));

        assertBusinessException(
                () -> engravingRequestService.cancelEngravingRequest(5L),
                ErrorCode.ENGRAVING_REQUEST_ALREADY_CANCELED
        );
    }

    /**
     * 용도: 테스트용 제품 옵션 생성.
     * 신청 생성과 목록 응답 변환에 사용할 제품과 옵션을 연결한다.
     */
    private ProductOption createProductOption(Long id) {
        Product product = new Product(ProductCategory.BAG, "가방 설명");
        ProductOption option = new ProductOption("L Aren 비세토스 스쿨 토트", "갈색", 1_250_000);
        product.addOption(option);
        ReflectionTestUtils.setField(option, "id", id);
        return option;
    }

    /**
     * 용도: 테스트용 보유 각인 생성.
     * 서로 다른 Before와 After 좌표를 가진 보유 각인을 생성한다.
     */
    private NightPathRecord createNightPathRecord(Long id, Long userId) {
        ConstellationShape before = new ConstellationShape(
                List.of(new ConstellationPoint(0, 1, 3)),
                List.of()
        );
        ConstellationShape after = new ConstellationShape(
                List.of(new ConstellationPoint(10, 40, 300)),
                List.of()
        );
        NightPathRecord engraving = new NightPathRecord(
                userId,
                100L + id,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "플레이 분석 코멘트",
                new ConstellationData(before, after)
        );
        ReflectionTestUtils.setField(engraving, "id", id);
        ReflectionTestUtils.setField(engraving, "createdAt", LocalDateTime.of(2026, 8, 7, 10, 30));
        return engraving;
    }

    /**
     * 용도: 테스트용 제품 각인 신청 생성.
     * 목록과 취소 테스트에 사용할 신청 Entity를 지정한 상태로 구성한다.
     */
    private EngravingRequest createEngravingRequest(Long id, EngravingRequestStatus status) {
        EngravingRequest request = new EngravingRequest(
                1L,
                createProductOption(3L),
                createNightPathRecord(1L, 1L),
                EngravingColor.GOLD,
                "NWdfw25"
        );
        ReflectionTestUtils.setField(request, "id", id);
        if (status == EngravingRequestStatus.CANCELED) {
            request.cancel();
        }
        return request;
    }

    /**
     * 용도: 비즈니스 예외 검증.
     * 실행 결과가 예상한 오류 코드를 가진 BusinessException인지 확인한다.
     */
    private void assertBusinessException(Runnable action, ErrorCode expectedErrorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(expectedErrorCode)
                );
    }
}
