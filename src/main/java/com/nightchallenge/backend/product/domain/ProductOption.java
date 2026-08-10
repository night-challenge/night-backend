package com.nightchallenge.backend.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 용도: 제품 옵션 정보 저장
 * 제품별 이름, 색상이나 용량 등의 구분값과 가격처럼 옵션마다 달라질 수 있는 정보를 관리한다.
 */
@Getter
@Entity
@Table(name = "product_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "option_name", nullable = false)
    private String optionName;

    @Column(name = "option_label")
    private String optionLabel;

    @Column(nullable = false)
    private int price;

    /**
     * 용도: 제품 옵션 생성
     * 옵션별 제품명, 구분값과 가격을 설정하며 소속 제품은 Product.addOption으로 연결한다.
     */
    public ProductOption(String optionName, String optionLabel, int price) {
        this.optionName = optionName;
        this.optionLabel = optionLabel;
        this.price = price;
    }

    /**
     * 용도: 소속 제품 설정
     * Product가 옵션을 추가할 때 외래 키 연관관계를 함께 설정한다.
     */
    void assignProduct(Product product) {
        this.product = product;
    }
}
