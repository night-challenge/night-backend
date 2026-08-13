package com.nightchallenge.backend.engravingrequest.repository;

import com.nightchallenge.backend.engravingrequest.domain.EngravingRequest;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 용도: 제품 각인 신청 데이터 접근.
 * 신청 저장과 사용자·상태별 최신순 목록 조회 및 제품 코드 중복 확인 기능을 제공한다.
 */
public interface EngravingRequestRepository extends JpaRepository<EngravingRequest, Long> {

    /**
     * 용도: 사용자 상태별 신청 목록 조회.
     * 고정 사용자의 신청을 상태로 필터링하고 생성일시 기준 최신순으로 반환한다.
     */
    List<EngravingRequest> findAllByUserIdAndStatusValueOrderByCreatedAtDesc(
            Long userId,
            String statusValue
    );

    /**
     * 용도: 사용자별 신청 상태 목록 조회.
     * 신청 상태 enum을 DB 저장 문자열로 변환해 최신 신청부터 조회한다.
     */
    default List<EngravingRequest> findAllByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            EngravingRequestStatus status
    ) {
        return findAllByUserIdAndStatusValueOrderByCreatedAtDesc(userId, status.getValue());
    }

    /**
     * 용도: 제품 코드 중복 확인.
     * 새 제품 코드를 발급하기 전에 동일한 코드가 저장되어 있는지 확인한다.
     */
    boolean existsByProductCode(String productCode);
}
