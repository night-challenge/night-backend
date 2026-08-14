package com.nightchallenge.backend.engravingrequest.service;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.dto.response.ConstellationShapeResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingSummaryResponse;
import com.nightchallenge.backend.engraving.repository.NightPathRecordRepository;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequest;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequestStatus;
import com.nightchallenge.backend.engravingrequest.dto.request.EngravingRequestCreateRequest;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestCreateResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestListResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestProductResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestSummaryResponse;
import com.nightchallenge.backend.engravingrequest.repository.EngravingRequestRepository;
import com.nightchallenge.backend.engravingrequest.support.ProductCodeGenerator;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import com.nightchallenge.backend.product.domain.ProductOption;
import com.nightchallenge.backend.product.repository.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 용도: 제품 각인 신청 비즈니스 로직.
 * 제품 옵션과 보유 각인을 연결한 신청 생성, 상태별 목록 조회와 신청 취소를 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EngravingRequestService {

    private static final Long TEMP_USER_ID = 1L;
    private static final String PRODUCT_NOT_FOUND_MESSAGE = "존재하지 않는 제품입니다.";

    private final EngravingRequestRepository engravingRequestRepository;
    private final ProductOptionRepository productOptionRepository;
    private final NightPathRecordRepository nightPathRecordRepository;
    private final ProductCodeGenerator productCodeGenerator;

    /**
     * 용도: 제품 각인 신청 생성.
     * 필수 선택값과 연관 데이터의 존재 여부를 검증하고 고유 제품 코드를 발급해 신청을 저장한다.
     */
    @Transactional
    public EngravingRequestCreateResponse createEngravingRequest(EngravingRequestCreateRequest request) {
        validateSelections(request);

        ProductOption productOption = productOptionRepository.findById(request.productOptionId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        PRODUCT_NOT_FOUND_MESSAGE
                ));
        NightPathRecord nightPathRecord = nightPathRecordRepository.findById(request.nightPathRecordId())
                .filter(record -> TEMP_USER_ID.equals(record.getUserId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENGRAVING_NOT_FOUND));
        String productCode = productCodeGenerator.generateUniqueCode(
                engravingRequestRepository::existsByProductCode
        );

        EngravingRequest engravingRequest = engravingRequestRepository.save(new EngravingRequest(
                TEMP_USER_ID,
                productOption,
                nightPathRecord,
                request.engravingColor(),
                productCode
        ));

        return new EngravingRequestCreateResponse(engravingRequest.getId(), engravingRequest.getProductCode());
    }

    /**
     * 용도: 상태별 제품 각인 신청 목록 조회.
     * 고정 사용자의 신청을 상태와 생성일시 기준으로 조회하고 최종 After 데이터가 포함된 응답으로 변환한다.
     */
    public EngravingRequestListResponse getEngravingRequests(EngravingRequestStatus status) {
        List<EngravingRequestSummaryResponse> records = engravingRequestRepository
                .findAllByUserIdAndStatusOrderByCreatedAtDesc(TEMP_USER_ID, status)
                .stream()
                .map(this::toSummaryResponse)
                .toList();

        return new EngravingRequestListResponse(records);
    }

    /**
     * 용도: 제품 각인 신청 취소.
     * 고정 사용자의 신청을 조회하고 이미 취소되지 않은 경우 데이터 삭제 없이 상태만 취소됨으로 변경한다.
     */
    @Transactional
    public void cancelEngravingRequest(Long engravingRequestId) {
        EngravingRequest engravingRequest = engravingRequestRepository.findById(engravingRequestId)
                .filter(request -> TEMP_USER_ID.equals(request.getUserId()))
                .orElseThrow(() -> new BusinessException(ErrorCode.ENGRAVING_REQUEST_NOT_FOUND));

        if (engravingRequest.getStatus() == EngravingRequestStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ENGRAVING_REQUEST_ALREADY_CANCELED);
        }

        engravingRequest.cancel();
    }

    /**
     * 용도: 신청 필수 선택값 검증.
     * 각인과 색상의 누락 조합을 구분하여 API 명세에 맞는 400 오류를 발생시킨다.
     */
    private void validateSelections(EngravingRequestCreateRequest request) {
        boolean engravingMissing = request.nightPathRecordId() == null;
        boolean colorMissing = request.engravingColor() == null;

        if (engravingMissing && colorMissing) {
            throw new BusinessException(ErrorCode.ENGRAVING_REQUEST_SELECTION_REQUIRED);
        }
        if (engravingMissing) {
            throw new BusinessException(ErrorCode.ENGRAVING_REQUEST_ENGRAVING_REQUIRED);
        }
        if (colorMissing) {
            throw new BusinessException(ErrorCode.ENGRAVING_REQUEST_COLOR_REQUIRED);
        }
        if (request.productOptionId() == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, PRODUCT_NOT_FOUND_MESSAGE);
        }
    }

    /**
     * 용도: 신청 목록 항목 응답 변환.
     * 신청 Entity의 제품 정보와 보유 각인의 최종 After 데이터를 목록 응답 구조로 변환한다.
     */
    private EngravingRequestSummaryResponse toSummaryResponse(EngravingRequest request) {
        ProductOption productOption = request.getProductOption();
        NightPathRecord engraving = request.getNightPathRecord();

        EngravingRequestProductResponse productResponse = new EngravingRequestProductResponse(
                productOption.getOptionName(),
                productOption.getOptionLabel()
        );
        EngravingSummaryResponse engravingResponse = new EngravingSummaryResponse(
                engraving.getId(),
                engraving.getConstellationName(),
                engraving.getKeywords(),
                engraving.getComment(),
                ConstellationShapeResponse.from(engraving.getConstellationData().after()),
                engraving.getCreatedAt()
        );

        return new EngravingRequestSummaryResponse(
                request.getId(),
                request.getProductCode(),
                productResponse,
                engravingResponse
        );
    }
}
