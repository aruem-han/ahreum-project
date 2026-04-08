package com.congestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지하철역 기본 정보 + 실시간 혼잡도를 담는 DTO.
 * StationRepository의 인메모리 데이터 및 API 응답에 사용된다.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Station {

    /** 역 고유 ID */
    private int id;

    /** 역명 (예: "강남") — 공공 API 조회 키로도 사용 */
    private String name;

    /** 호선 (예: "2호선", "9호선") — 프론트 색상 코드와 매핑 */
    private String line;

    /** 대표 방향 (예: "내선", "외선", "상행", "하행") */
    private String direction;

    /**
     * 현재 혼잡도 비율 (0~100).
     * PublicApiService.estimateCongestion() 결과로 채워진다.
     * 40 미만: 여유 / 65 미만: 보통 / 85 미만: 혼잡 / 85 이상: 매우 혼잡
     */
    private int congestionRate;
}
