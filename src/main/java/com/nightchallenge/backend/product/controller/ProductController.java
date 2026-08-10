package com.nightchallenge.backend.product.controller;

import com.nightchallenge.backend.global.response.ApiResponse;
import com.nightchallenge.backend.product.dto.response.ProductOptionDetailResponse;
import com.nightchallenge.backend.product.dto.response.ProductOptionListResponse;
import com.nightchallenge.backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용도: 제품 조회 HTTP 요청 처리.
 * 제품 목록과 옵션 상세 조회 요청을 Service에 전달하고 공통 ApiResponse 형식으로 반환한다.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 용도: 카테고리별 제품 옵션 목록 API.
     * 한글 카테고리 query parameter를 전달받아 해당 옵션 목록을 조회한다.
     */
    @GetMapping
    public ApiResponse<ProductOptionListResponse> getProductOptions(
            @RequestParam(required = false) String category
    ) {
        return ApiResponse.success(productService.getProductOptions(category));
    }

    /**
     * 용도: 제품 옵션 상세 API.
     * URL 경로의 optionId로 선택한 제품 옵션과 공통 제품 설명을 조회한다.
     */
    @GetMapping("/options/{optionId}")
    public ApiResponse<ProductOptionDetailResponse> getProductOption(
            @PathVariable Long optionId
    ) {
        return ApiResponse.success(productService.getProductOption(optionId));
    }
}
