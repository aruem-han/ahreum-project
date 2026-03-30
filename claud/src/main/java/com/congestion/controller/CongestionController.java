package com.congestion.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.congestion.service.PublicApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CongestionController {

    private final PublicApiService publicApiService;

    // 실시간 도착 정보 조회
@GetMapping("/arrival/{stationName}")
public ResponseEntity<List<Map<String, Object>>> arrival(@PathVariable String stationName) {
    return ResponseEntity.ok(publicApiService.fetchSubwayCongestion(stationName));
}
    
}
