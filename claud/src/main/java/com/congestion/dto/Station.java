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


    private int id;

    private String name;

    private String line;

    private String direction;

    private int congestionRate;

    private boolean isFavorite;
}
