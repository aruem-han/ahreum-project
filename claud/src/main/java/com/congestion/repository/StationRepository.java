package com.congestion.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.congestion.dto.Station;

/**
 * 지하철역 정보 MyBatis Mapper 인터페이스.
 */
@Mapper
public interface StationRepository {

    /**
     * 즐겨찾기 한 역 ID 목록 반환
     * @return 즐겨찾기 한 역의 ID(int) 목록
     */
    List<Integer> findFavoriteStationIds();

    /**
     * 즐겨찾기 추가
     * @param stationId 역 ID
     */
    void addFavorite(@Param("stationId") int stationId);

    /**
     * 즐겨찾기 삭제
     * @param stationId 역 ID
     */
    void removeFavorite(@Param("stationId") int stationId);
}
