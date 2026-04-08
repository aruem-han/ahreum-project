package com.congestion.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 혼잡도 분석에 필요한 컨텍스트 정보를 담는 DTO.
 * ClaudeService가 이 객체를 받아 시스템 프롬프트와 사용자 프롬프트를 구성한다.
 */
@Getter
@Setter
@Builder
public class CongestionContext {

    /** 사용자가 입력한 자연어 질문 (예: "지금 2호선 강남역 얼마나 붐비나요?") */
    private String userQuery;

    /** 역명 (예: "강남") — null이면 전체 노선 일반 답변으로 분기 */
    private String stationName;

    /** 호선 (예: "2호선", "9호선") */
    private String line;

    /** 열차 방향 (예: "내선", "외선", "상행", "하행") */
    private String direction;

    /** 현재 혼잡도 비율 0~100 (PublicApiService.estimateCongestion 결과) */
    private int currentRate;

    /** 조회 시각 (기본값: LocalDateTime.now()) */
    private LocalDateTime currentTime;

    /** 요일 (예: "화요일") — Claude가 요일 패턴을 학습에 활용 */
    private String dayOfWeek;

    /** 날씨 상태 (예: "맑음", "비") — 기상청 API 결과 또는 기본값 */
    private String weather;

    /**
     * 최근 1시간 혼잡도 추이 (30분 단위 리스트).
     * 예: [45, 62, 78, 85] → 18:00=45%, 18:30=62%, 19:00=78%, 19:30=85%
     */
    private List<Integer> recentTrend;

    /**
     * 같은 요일의 과거 시간대별 평균 혼잡도.
     * 예: {"07시": 72, "08시": 88, "09시": 65}
     */
    private Map<String, Integer> historicalAvg;
}
