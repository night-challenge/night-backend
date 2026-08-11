package com.nightchallenge.backend.engraving.repository;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 용도: 각인(별자리) 데이터 접근.
 * NightPathRecord의 저장과 조회를 제공하고, 생성일시 기준 정렬 목록 조회를 지원한다.
 */
public interface NightPathRecordRepository extends JpaRepository<NightPathRecord, Long> {

    /**
     * 용도: 사용자별 각인 목록 조회.
     * 최신 각인이 먼저 오도록 생성일시 내림차순으로 정렬해 반환한다.
     */
    List<NightPathRecord> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}