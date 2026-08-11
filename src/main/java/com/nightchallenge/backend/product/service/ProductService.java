package com.nightchallenge.backend.product.service;

import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import com.nightchallenge.backend.product.domain.ProductCategory;
import com.nightchallenge.backend.product.domain.ProductOption;
import com.nightchallenge.backend.product.dto.response.ProductOptionDetailResponse;
import com.nightchallenge.backend.product.dto.response.ProductOptionListResponse;
import com.nightchallenge.backend.product.repository.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 용도: 제품 조회 비즈니스 로직.
 * 카테고리 검증과 제품 옵션 조회를 수행하고 Entity를 명세의 응답 DTO로 변환한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private static final String INVALID_CATEGORY_MESSAGE = "존재하지 않는 카테고리입니다.";
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "존재하지 않는 제품입니다.";

    private final ProductOptionRepository productOptionRepository;

    /**
     * 용도: 카테고리별 제품 옵션 목록 조회.
     * 한글 카테고리를 검증한 뒤 해당 카테고리의 옵션을 ID 오름차순으로 조회해 반환한다.
     */
    public ProductOptionListResponse getProductOptions(String categoryName) {
        ProductCategory category = parseCategory(categoryName);
        List<ProductOption> options = productOptionRepository
                .findAllByProductCategoryOrderByIdAsc(category);

        return ProductOptionListResponse.from(options);
    }

    /**
     * 용도: 제품 옵션 상세 조회.
     * optionId와 일치하는 옵션을 조회하고 없으면 404 비즈니스 예외를 발생시킨다.
     */
    public ProductOptionDetailResponse getProductOption(Long optionId) {
        ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        PRODUCT_NOT_FOUND_MESSAGE
                ));

        return ProductOptionDetailResponse.from(option);
    }

    /**
     * 용도: 요청 카테고리 검증 및 변환.
     * 허용된 네 가지 한글 카테고리가 아니거나 값이 없으면 400 비즈니스 예외를 발생시킨다.
     */
    private ProductCategory parseCategory(String categoryName) {
        return ProductCategory.fromDisplayName(categoryName)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_REQUEST,
                        INVALID_CATEGORY_MESSAGE
                ));
    }
}
