package com.congestion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.congestion.dto.Station;
import com.congestion.service.PublicApiService;
import com.congestion.service.StationService;

import lombok.RequiredArgsConstructor;

/**
 * 지하철 혼잡도 및 역 정보 REST 컨트롤러.
 *
 * 프론트엔드 api/congestion.js 와 1:1 매핑되는 엔드포인트를 제공한다.
 * Vite 프록시 설정(/api → 백엔드 루트)으로 인해 프론트 /api/xxx → 백엔드 /xxx 로 전달된다.
 */
@RestController
@RequiredArgsConstructor
public class CongestionController {

    private final PublicApiService publicApiService;
    private final StationService stationService;

    /**
     * 서울 열린데이터광장 지하철 실시간 도착 정보 직접 조회.
     * GET /arrival/{stationName}
     * 예: /arrival/강남 → 강남역 열차 도착 목록 반환
     */
    @GetMapping("/arrival/{stationName}")
    public ResponseEntity<List<Map<String, Object>>> arrival(@PathVariable String stationName) {
        return ResponseEntity.ok(publicApiService.fetchSubwayCongestion(stationName));
    }

    /**
     * 역명 키워드 검색.
     * GET /stations/search?q={키워드}
     * 예: /stations/search?q=강남 → 이름에 "강남"이 포함된 역 목록 반환
     * 반환 데이터에 실시간 혼잡도(congestionRate)가 포함된다.
     */
    @GetMapping("/stations/search")
    public ResponseEntity<List<Station>> searchStations(@RequestParam String q) {
        return ResponseEntity.ok(stationService.searchStations(q));
    }

    /**
     * 즐겨찾기 역 목록 조회.
     * GET /stations/favorites
     * 현재 고정 4개(강남, 고속터미널, 홍대입구, 잠실) — 실시간 혼잡도 적용.
     * 추후 사용자 인증 추가 시 userId 기반 즐겨찾기로 교체 예정.
     */
    @GetMapping("/stations/favorites")
    public ResponseEntity<List<Station>> getFavorites() {
        return ResponseEntity.ok(stationService.getFavorites());
    }

    /**
     * 특정 역 혼잡도 단건 조회.
     * GET /congestion/{stationId}
     * 예: /congestion/1 → 강남역 현재 혼잡도 반환
     * 존재하지 않는 ID면 404 반환.
     */
    @GetMapping("/congestion/{stationId}")
    public ResponseEntity<Station> getCongestion(@PathVariable int stationId) {
        return stationService.getCongestionById(stationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
