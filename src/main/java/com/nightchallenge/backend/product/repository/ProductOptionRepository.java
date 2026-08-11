package com.nightchallenge.backend.product.repository;

import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 용도: 제품 옵션 데이터 접근
 * 제품 옵션 저장과 단건 조회를 제공하고 제품 카테고리를 기준으로 옵션 목록을 조회한다.
 */
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    /**
     * 용도: 카테고리별 제품 옵션 조회
     * 제품 목록 API에 필요한 옵션을 소속 제품의 카테고리로 조회하여 ID 오름차순으로 반환한다.
     */
    List<ProductOption> findAllByProductCategoryOrderByIdAsc(ProductCategory category);
}
