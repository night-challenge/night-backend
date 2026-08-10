package com.nightchallenge.backend.product.repository;

import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: 제품 Repository 동작 검증
 * H2 테스트 DB에서 제품과 옵션의 연관관계 및 카테고리별 조회가 명세대로 동작하는지 확인한다.
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRepository productOptionRepository;

    @Test
    @DisplayName("제품을 저장하면 연결된 옵션도 함께 저장한다")
    void saveProductWithOptions() {
        // 제품과 옵션을 양방향으로 연결한 뒤 제품만 저장해 옵션까지 영속화되는지 검증한다.
        Product product = new Product(ProductCategory.BAG, "비세토스 모노그램 캔버스 토트백");
        product.addOption(new ProductOption("Aren 비세토스 스쿨 토트", "갈색", 1_250_000));
        product.addOption(new ProductOption("Aren 비세토스 스쿨 토트", "분홍", 1_250_000));

        Product savedProduct = productRepository.saveAndFlush(product);

        assertThat(savedProduct.getId()).isNotNull();
        assertThat(productOptionRepository.findAll())
                .hasSize(2)
                .allSatisfy(option -> assertThat(option.getProduct().getId()).isEqualTo(savedProduct.getId()));
    }

    @Test
    @DisplayName("제품 카테고리로 해당 제품만 조회한다")
    void findProductsByCategory() {
        // 서로 다른 카테고리의 제품을 저장하고 요청한 카테고리만 반환하는지 검증한다.
        productRepository.save(new Product(ProductCategory.BAG, "가방 설명"));
        productRepository.save(new Product(ProductCategory.TRAVEL, "트래블 설명"));
        productRepository.flush();

        List<Product> products = productRepository.findAllByCategoryOrderByIdAsc(ProductCategory.BAG);

        assertThat(products)
                .hasSize(1)
                .first()
                .satisfies(product -> {
                    assertThat(product.getCategory()).isEqualTo(ProductCategory.BAG);
                    assertThat(product.getDescription()).isEqualTo("가방 설명");
                });
    }

    @Test
    @DisplayName("제품 카테고리로 해당 옵션만 조회한다")
    void findProductOptionsByCategory() {
        // 제품 목록 API가 카테고리에 해당하는 옵션만 가져올 수 있는지 검증한다.
        Product bag = new Product(ProductCategory.BAG, "가방 설명");
        bag.addOption(new ProductOption("Aren 비세토스 스쿨 토트", "갈색", 1_250_000));
        bag.addOption(new ProductOption("Aren 비세토스 스쿨 토트", "분홍", 1_250_000));

        Product travel = new Product(ProductCategory.TRAVEL, "트래블 설명");
        travel.addOption(new ProductOption("L 비세토스 수트케이스", "갈색", 2_150_000));

        productRepository.saveAllAndFlush(List.of(bag, travel));

        List<ProductOption> options = productOptionRepository
                .findAllByProductCategoryOrderByIdAsc(ProductCategory.BAG);

        assertThat(options)
                .extracting(ProductOption::getOptionLabel)
                .containsExactly("갈색", "분홍");
        assertThat(options)
                .allSatisfy(option -> assertThat(option.getProduct().getCategory()).isEqualTo(ProductCategory.BAG));
    }
}
