package com.nightchallenge.backend.product.service;

import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import com.nightchallenge.backend.product.dto.response.ProductOptionDetailResponse;
import com.nightchallenge.backend.product.dto.response.ProductOptionListResponse;
import com.nightchallenge.backend.product.repository.ProductOptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 용도: 제품 조회 Service 단위 테스트.
 * Repository를 대체한 Mock으로 카테고리 검증, DTO 변환과 예외 처리 규칙을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductOptionRepository productOptionRepository;

    @InjectMocks
    private ProductService productService;

    @ParameterizedTest
    @CsvSource({
            "가방, BAG",
            "트래블, TRAVEL",
            "패션소품, FASHION_ACCESSORY",
            "라이프스타일, LIFESTYLE"
    })
    @DisplayName("허용된 한글 카테고리를 enum으로 변환해 옵션을 조회한다")
    void getProductOptionsWithAllowedCategory(String categoryName, ProductCategory category) {
        given(productOptionRepository.findAllByProductCategoryOrderByIdAsc(category))
                .willReturn(List.of());

        ProductOptionListResponse response = productService.getProductOptions(categoryName);

        assertThat(response.options()).isEmpty();
        verify(productOptionRepository).findAllByProductCategoryOrderByIdAsc(category);
    }

    @Test
    @DisplayName("카테고리별 옵션 목록을 명세의 DTO로 변환한다")
    void getProductOptions() {
        Product product = new Product(ProductCategory.BAG, "가방 설명");
        ProductOption option = new ProductOption("Aren 비세토스 스쿨 토트", "갈색", 1_250_000);
        product.addOption(option);
        ReflectionTestUtils.setField(option, "id", 1L);

        given(productOptionRepository.findAllByProductCategoryOrderByIdAsc(ProductCategory.BAG))
                .willReturn(List.of(option));

        ProductOptionListResponse response = productService.getProductOptions("가방");

        assertThat(response.options()).hasSize(1);
        assertThat(response.options().get(0).id()).isEqualTo(1L);
        assertThat(response.options().get(0).optionName()).isEqualTo("Aren 비세토스 스쿨 토트");
        assertThat(response.options().get(0).optionLabel()).isEqualTo("갈색");
        assertThat(response.options().get(0).price()).isEqualTo(1_250_000);
    }

    @Test
    @DisplayName("조회 결과가 없으면 options 빈 배열을 반환한다")
    void getProductOptionsReturnsEmptyOptions() {
        given(productOptionRepository.findAllByProductCategoryOrderByIdAsc(ProductCategory.TRAVEL))
                .willReturn(List.of());

        ProductOptionListResponse response = productService.getProductOptions("트래블");

        assertThat(response.options()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("잘못된 카테고리는 400 비즈니스 예외를 발생시킨다")
    void getProductOptionsWithInvalidCategory() {
        assertThatThrownBy(() -> productService.getProductOptions("식품"))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                    assertThat(exception.getMessage()).isEqualTo("존재하지 않는 카테고리입니다.");
                });

        verifyNoInteractions(productOptionRepository);
    }

    @Test
    @DisplayName("카테고리가 없으면 400 비즈니스 예외를 발생시킨다")
    void getProductOptionsWithoutCategory() {
        assertThatThrownBy(() -> productService.getProductOptions(null))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
                    assertThat(exception.getMessage()).isEqualTo("존재하지 않는 카테고리입니다.");
                });

        verifyNoInteractions(productOptionRepository);
    }

    @Test
    @DisplayName("제품 옵션 상세 정보를 명세의 DTO로 변환한다")
    void getProductOption() {
        Product product = new Product(ProductCategory.TRAVEL, "여행에 적합한 수트케이스입니다.");
        ProductOption option = new ProductOption("L 비세토스 수트케이스", "갈색", 2_150_000);
        product.addOption(option);
        ReflectionTestUtils.setField(option, "id", 5L);

        given(productOptionRepository.findById(5L)).willReturn(Optional.of(option));

        ProductOptionDetailResponse response = productService.getProductOption(5L);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.optionName()).isEqualTo("L 비세토스 수트케이스");
        assertThat(response.optionLabel()).isEqualTo("갈색");
        assertThat(response.price()).isEqualTo(2_150_000);
        assertThat(response.description()).isEqualTo("여행에 적합한 수트케이스입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 optionId는 404 비즈니스 예외를 발생시킨다")
    void getProductOptionNotFound() {
        given(productOptionRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductOption(999L))
                .isInstanceOfSatisfying(BusinessException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
                    assertThat(exception.getMessage()).isEqualTo("존재하지 않는 제품입니다.");
                });
    }
}
