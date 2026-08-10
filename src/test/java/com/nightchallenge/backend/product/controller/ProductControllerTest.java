package com.nightchallenge.backend.product.controller;

import com.nightchallenge.backend.global.exception.GlobalExceptionHandler;
import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import com.nightchallenge.backend.product.repository.ProductOptionRepository;
import com.nightchallenge.backend.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 용도: 제품 Controller HTTP 응답 검증.
 * 실제 Service와 전역 예외 처리기를 연결해 URL, 공통 응답 형식과 오류 상태를 검증한다.
 */
class ProductControllerTest {

    private ProductOptionRepository productOptionRepository;
    private MockMvc mockMvc;

    /**
     * 용도: Controller 테스트 환경 구성.
     * Mock Repository를 사용하는 Service와 전역 예외 처리기를 MockMvc에 연결한다.
     */
    @BeforeEach
    void setUp() {
        productOptionRepository = mock(ProductOptionRepository.class);
        ProductService productService = new ProductService(productOptionRepository);
        ProductController productController = new ProductController(productService);

        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("카테고리별 제품 옵션 목록을 공통 성공 응답으로 반환한다")
    void getProductOptions() throws Exception {
        Product product = new Product(ProductCategory.BAG, "가방 설명");
        ProductOption option = new ProductOption("L Aren 비세토스 스쿨 토트", "갈색", 1_250_000);
        product.addOption(option);
        ReflectionTestUtils.setField(option, "id", 1L);

        given(productOptionRepository.findAllByProductCategoryOrderByIdAsc(ProductCategory.BAG))
                .willReturn(List.of(option));

        mockMvc.perform(get("/api/products").queryParam("category", "가방"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.options[0].id").value(1))
                .andExpect(jsonPath("$.data.options[0].optionName").value("L Aren 비세토스 스쿨 토트"))
                .andExpect(jsonPath("$.data.options[0].optionLabel").value("갈색"))
                .andExpect(jsonPath("$.data.options[0].price").value(1_250_000));
    }

    @Test
    @DisplayName("조회 결과가 없으면 options 빈 배열을 반환한다")
    void getProductOptionsReturnsEmptyArray() throws Exception {
        given(productOptionRepository.findAllByProductCategoryOrderByIdAsc(ProductCategory.LIFESTYLE))
                .willReturn(List.of());

        mockMvc.perform(get("/api/products").queryParam("category", "라이프스타일"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.options").isArray())
                .andExpect(jsonPath("$.data.options").isEmpty());
    }

    @Test
    @DisplayName("잘못된 카테고리는 공통 400 오류 응답으로 반환한다")
    void getProductOptionsWithInvalidCategory() throws Exception {
        mockMvc.perform(get("/api/products").queryParam("category", "식품"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 카테고리입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("누락된 카테고리는 공통 400 오류 응답으로 반환한다")
    void getProductOptionsWithoutCategory() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 카테고리입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("제품 옵션 상세 정보를 공통 성공 응답으로 반환한다")
    void getProductOption() throws Exception {
        Product product = new Product(ProductCategory.TRAVEL, "여행에 적합한 수트케이스입니다.");
        ProductOption option = new ProductOption("L 비세토스 수트케이스", "갈색", 6_750_000);
        product.addOption(option);
        ReflectionTestUtils.setField(option, "id", 5L);

        given(productOptionRepository.findById(5L)).willReturn(Optional.of(option));

        mockMvc.perform(get("/api/products/options/{optionId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.optionName").value("L 비세토스 수트케이스"))
                .andExpect(jsonPath("$.data.optionLabel").value("갈색"))
                .andExpect(jsonPath("$.data.price").value(6_750_000))
                .andExpect(jsonPath("$.data.description").value("여행에 적합한 수트케이스입니다."));
    }

    @Test
    @DisplayName("존재하지 않는 optionId는 공통 404 오류 응답으로 반환한다")
    void getProductOptionNotFound() throws Exception {
        given(productOptionRepository.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/products/options/{optionId}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 제품입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
