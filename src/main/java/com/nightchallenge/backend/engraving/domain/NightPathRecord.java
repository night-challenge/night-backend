package com.nightchallenge.backend.engraving.domain;

import com.nightchallenge.backend.engraving.domain.converter.ConstellationDataJsonConverter;
import com.nightchallenge.backend.engraving.domain.converter.StringListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 용도: 각인(별자리) 데이터 저장.
 * 게임 승리로 생성된 나이트 이동 궤적과 AI 분석 결과, 재구성된 최종 별자리를 하나의 각인으로 관리한다.
 */
@Getter
@Entity
@Table(name = "night_path_records")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NightPathRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소유자 식별자. 로그인 기능 도입 전까지는 고정값(1)을 사용한다.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "constellation_name", nullable = false)
    private String constellationName;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "keywords", nullable = false, columnDefinition = "JSON")
    private List<String> keywords;

    @Column(name = "comment", nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Convert(converter = ConstellationDataJsonConverter.class)
    @Column(name = "constellation_data", nullable = false, columnDefinition = "JSON")
    private ConstellationData constellationData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 용도: 각인 생성.
     * 게임 승리 후 AI 분석과 별자리 재구성을 마친 결과로 각인 하나를 생성한다.
     */
    public NightPathRecord(
            Long userId,
            String constellationName,
            List<String> keywords,
            String comment,
            ConstellationData constellationData
    ) {
        this.userId = userId;
        this.constellationName = constellationName;
        this.keywords = keywords;
        this.comment = comment;
        this.constellationData = constellationData;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 용도: 각인 이름 수정.
     * 기존 이름과 동일하지 않은 새 이름으로 각인 이름을 변경한다. 동일 여부 검증은 Service에서 처리한다.
     */
    public void rename(String newName) {
        this.constellationName = newName;
    }

    /**
     * 용도: 별자리 재생성 반영.
     * before 원본 궤적은 유지한 채 재구성 로직으로 새로 만든 after 데이터로 교체한다.
     */
    public void regenerateAfter(ConstellationShape newAfter) {
        this.constellationData = new ConstellationData(this.constellationData.before(), newAfter);
    }
}