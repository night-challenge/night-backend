package com.nightchallenge.backend.product.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 용도: 제품 공통 정보 저장.
 * 옵션에 따라 달라지지 않는 카테고리와 제품 설명을 관리하고 여러 제품 옵션을 하나의 제품으로 묶는다.
 */
@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private ProductCategory category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> options = new ArrayList<>();

    /**
     * 용도: 제품 생성.
     * 제품 옵션들이 공통으로 사용하는 카테고리와 설명을 설정한다.
     */
    public Product(ProductCategory category, String description) {
        this.category = category;
        this.description = description;
    }

    /**
     * 용도: 제품 옵션 연결.
     * 제품과 옵션의 양방향 연관관계를 한 번에 설정하여 서로 다른 상태로 저장되는 것을 방지한다.
     */
    public void addOption(ProductOption option) {
        options.add(option);
        option.assignProduct(this);
    }

    /**
     * 용도: 제품 옵션 목록 조회.
     * 외부에서 연관관계 컬렉션을 직접 변경하지 못하도록 읽기 전용 목록을 반환한다.
     */
    public List<ProductOption> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
