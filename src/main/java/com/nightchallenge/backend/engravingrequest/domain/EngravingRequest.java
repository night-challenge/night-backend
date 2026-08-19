package com.nightchallenge.backend.engravingrequest.domain;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.product.domain.ProductOption;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 용도: 제품 각인 신청 정보 저장.
 * 사용자가 선택한 제품 옵션, 보유 각인과 색상을 연결하고 신청 상태와 제품 코드를 관리한다.
 */
@Getter
@Entity
@Table(
        name = "engraving_requests",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_engraving_requests_product_code",
                columnNames = "product_code"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EngravingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "night_path_record_id", nullable = false)
    private NightPathRecord nightPathRecord;

    @Column(name = "engraving_color", nullable = false)
    private String engravingColorValue;

    @Column(name = "product_code", nullable = false, unique = true, length = 7)
    private String productCode;

    @Column(name = "status", nullable = false)
    private String statusValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 용도: 제품 각인 신청 생성.
     * 사용자와 선택한 제품 옵션·각인·색상·제품 코드를 연결하고 신청 완료 상태와 생성일시를 설정한다.
     */
    public EngravingRequest(
            Long userId,
            ProductOption productOption,
            NightPathRecord nightPathRecord,
            EngravingColor engravingColor,
            String productCode
    ) {
        this.userId = userId;
        this.productOption = productOption;
        this.nightPathRecord = nightPathRecord;
        this.engravingColorValue = engravingColor.getValue();
        this.productCode = productCode;
        this.statusValue = EngravingRequestStatus.COMPLETED.getValue();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 용도: 각인 색상 조회.
     * DB에 저장된 명세 문자열을 각인 색상 enum으로 변환해 반환한다.
     */
    public EngravingColor getEngravingColor() {
        return EngravingColor.fromValue(engravingColorValue);
    }

    /**
     * 용도: 신청 상태 조회.
     * DB에 저장된 명세 문자열을 신청 상태 enum으로 변환해 반환한다.
     */
    public EngravingRequestStatus getStatus() {
        return EngravingRequestStatus.fromValue(statusValue);
    }

    /**
     * 용도: 제품 각인 신청 취소.
     * 신청 데이터와 제품 코드는 유지하고 신청 상태만 취소됨으로 변경한다.
     */
    public void cancel() {
        this.statusValue = EngravingRequestStatus.CANCELED.getValue();
    }
}
