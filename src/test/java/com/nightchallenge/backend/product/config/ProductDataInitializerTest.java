package com.nightchallenge.backend.product.config;

import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import com.nightchallenge.backend.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.ApplicationArguments;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 용도: 제품 초기 데이터 등록 검증.
 * 명세의 네 카테고리 옵션 구성과 서버 재실행 시 중복 저장 방지 동작을 검증한다.
 */
class ProductDataInitializerTest {

    @Test
    @DisplayName("네 카테고리의 초기 제품과 옵션을 등록한다")
    void initializesProducts() {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductDataInitializer initializer = new ProductDataInitializer(productRepository);

        initializer.run(mock(ApplicationArguments.class));

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository, times(4)).save(productCaptor.capture());

        List<Product> products = productCaptor.getAllValues();
        assertThat(products)
                .extracting(Product::getCategory)
                .containsExactly(
                        ProductCategory.BAG,
                        ProductCategory.TRAVEL,
                        ProductCategory.FASHION_ACCESSORY,
                        ProductCategory.LIFESTYLE
                );

        Product bag = products.get(0);
        assertThat(bag.getDescription()).contains("24K 도금 로고 브라스 플레이트");
        assertThat(bag.getOptions())
                .extracting(ProductOption::getOptionLabel)
                .containsExactly("갈색", "분홍", "검정");
        assertThat(bag.getOptions())
                .extracting(ProductOption::getOptionName)
                .containsOnly("L Aren 비세토스 스쿨 토트");
        assertThat(bag.getOptions())
                .extracting(ProductOption::getPrice)
                .containsOnly(1_250_000);

        Product travel = products.get(1);
        assertThat(travel.getDescription()).contains("라지 하드케이스 비세토스 수트케이스");
        assertThat(travel.getOptions())
                .extracting(ProductOption::getOptionLabel)
                .containsExactly("갈색", "분홍");
        assertThat(travel.getOptions())
                .extracting(ProductOption::getOptionName)
                .containsOnly("L 비세토스 수트케이스");
        assertThat(travel.getOptions())
                .extracting(ProductOption::getPrice)
                .containsOnly(6_750_000);

        Product fashionAccessory = products.get(2);
        assertThat(fashionAccessory.getDescription()).contains("자신만의 빛으로 세상을 밝히는 이들을 위한 향수");
        assertThat(fashionAccessory.getOptions())
                .extracting(ProductOption::getOptionName)
                .containsExactly(
                        "코스믹 스타 오 드 퍼퓸 50ml",
                        "코스믹 스타 오 드 퍼퓸 75ml"
                );
        assertThat(fashionAccessory.getOptions())
                .extracting(ProductOption::getOptionLabel)
                .containsExactly("50ml", "75ml");
        assertThat(fashionAccessory.getOptions())
                .extracting(ProductOption::getPrice)
                .containsExactly(118_000, 141_000);

        Product lifestyle = products.get(3);
        assertThat(lifestyle.getDescription()).contains("헤리티지 모노그램이 돋보이는 모바일 액세서리 케이스");
        assertThat(lifestyle.getOptions()).singleElement().satisfies(option -> {
            assertThat(option.getOptionName())
                    .isEqualTo("엠보스드 모노그램 레더 에어팟 프로 케이스");
            assertThat(option.getOptionLabel()).isNull();
            assertThat(option.getPrice()).isEqualTo(310_000);
        });
    }

    @Test
    @DisplayName("카테고리 데이터가 이미 있으면 다시 등록하지 않는다")
    void skipsExistingProducts() {
        ProductRepository productRepository = mock(ProductRepository.class);
        ProductDataInitializer initializer = new ProductDataInitializer(productRepository);
        given(productRepository.existsByCategory(any(ProductCategory.class))).willReturn(true);

        initializer.run(mock(ApplicationArguments.class));

        verify(productRepository, never()).save(any(Product.class));
    }
}
