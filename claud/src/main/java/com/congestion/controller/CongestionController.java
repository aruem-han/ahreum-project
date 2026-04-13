package com.congestion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/stations/search")
    public ResponseEntity<List<Station>> searchStations(@RequestParam String q) {
        return ResponseEntity.ok(stationService.searchStations(q));
    }

    @GetMapping("/stations/favorites")
    public ResponseEntity<List<Station>> getFavorites() {
        return ResponseEntity.ok(stationService.getFavorites());
    }

   
    @GetMapping("/congestion/{stationId}")
    public ResponseEntity<Station> getCongestion(@PathVariable int stationId) {
        return stationService.getCongestionById(stationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/stations/favorites/{stationId}")
    public ResponseEntity<Void> addFavorite(@PathVariable int stationId) {
        stationService.addFavorite(stationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/stations/favorites/{stationId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable int stationId) {
        stationService.removeFavorite(stationId);
        return ResponseEntity.ok().build();
    }
}
