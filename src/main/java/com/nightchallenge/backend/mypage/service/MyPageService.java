package com.nightchallenge.backend.mypage.service;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.dto.response.ConstellationShapeResponse;
import com.nightchallenge.backend.engraving.repository.NightPathRecordRepository;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequestStatus;
import com.nightchallenge.backend.engravingrequest.repository.EngravingRequestRepository;
import com.nightchallenge.backend.mypage.dto.response.EngravingCardListResponse;
import com.nightchallenge.backend.mypage.dto.response.EngravingCardSummaryResponse;
import com.nightchallenge.backend.mypage.dto.response.MyPageMainResponse;
import com.nightchallenge.backend.mypage.dto.response.RecentCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 용도: 마이페이지 조회 로직.
 * 기존 신청과 보유 각인 데이터를 조합해 마이페이지 메인 정보와 카드 모음을 제공한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private static final Long TEMP_USER_ID = 1L;
    private static final String TEMP_NICKNAME = "사자후";
    private static final String TEMP_USER_ID_DISPLAY = "sajahoo";

    private final NightPathRecordRepository nightPathRecordRepository;
    private final EngravingRequestRepository engravingRequestRepository;

    /**
     * 용도: 마이페이지 메인 조회.
     * 고정 사용자의 신청 완료 여부와 가장 최근에 생성된 카드 정보를 사용자 표시 정보와 함께 반환한다.
     */
    public MyPageMainResponse getMyPageMain() {
        boolean hasEngravingRequest = engravingRequestRepository.existsByUserIdAndStatus(
                TEMP_USER_ID,
                EngravingRequestStatus.COMPLETED
        );
        RecentCardResponse recentCard = nightPathRecordRepository
                .findFirstByUserIdOrderByCreatedAtDesc(TEMP_USER_ID)
                .map(this::toRecentCardResponse)
                .orElse(null);

        return new MyPageMainResponse(
                TEMP_NICKNAME,
                TEMP_USER_ID_DISPLAY,
                hasEngravingRequest,
                recentCard
        );
    }

    /**
     * 용도: 각인 카드 모음 조회.
     * 고정 사용자의 보유 각인을 생성일시 최신순으로 조회해 최종 After 데이터가 포함된 카드 목록으로 반환한다.
     */
    public EngravingCardListResponse getEngravingCards() {
        List<EngravingCardSummaryResponse> cards = nightPathRecordRepository
                .findAllByUserIdOrderByCreatedAtDesc(TEMP_USER_ID)
                .stream()
                .map(this::toCardSummaryResponse)
                .toList();

        return new EngravingCardListResponse(cards);
    }

    /**
     * 용도: 최근 카드 응답 변환.
     * 가장 최근에 생성된 각인의 식별자와 별자리 이름을 메인 화면용 응답으로 변환한다.
     */
    private RecentCardResponse toRecentCardResponse(NightPathRecord engraving) {
        return new RecentCardResponse(engraving.getId(), engraving.getConstellationName());
    }

    /**
     * 용도: 카드 목록 응답 변환.
     * 보유 각인의 기본 정보와 최종 After 좌표만 카드 모음 화면용 응답으로 변환한다.
     */
    private EngravingCardSummaryResponse toCardSummaryResponse(NightPathRecord engraving) {
        return new EngravingCardSummaryResponse(
                engraving.getId(),
                engraving.getConstellationName(),
                ConstellationShapeResponse.from(engraving.getConstellationData().after()),
                engraving.getCreatedAt()
        );
    }
}
