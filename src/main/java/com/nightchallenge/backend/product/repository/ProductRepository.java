package com.nightchallenge.backend.product.repository;

import com.nightchallenge.backend.product.domain.Product;
import com.nightchallenge.backend.product.domain.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 용도: 제품 데이터 접근
 * 제품 저장과 단건 조회를 제공하고 카테고리에 속한 제품을 조회한다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 용도: 카테고리별 제품 조회
     * 전달받은 카테고리에 속한 제품 그룹을 ID 오름차순으로 반환한다.
     */
    List<Product> findAllByCategoryOrderByIdAsc(ProductCategory category);
}
