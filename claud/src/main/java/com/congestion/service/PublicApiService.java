package com.congestion.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicApiService {

    private final RestTemplate restTemplate;
    @Value("${app.public-api.seoul-key}")
    private String seoulKey;

    //서울 지하철 실시간 혼잡도 api호출

    public int fetchSeoulSubwayCongestion(String stationCode, String line) {
        String url = "http://swopenapi.seoul.go.kr/api/subway/%s/json/realtimeStationArrival/0/10/%s"
        .formatted(seoulKey, stationCode);

        try{
            Map response = restTemplate.getForObject(url, Map.class);
            
            // 혼잡도 필드 추출 
            List<Map<String, Object>> rows =
                (List<Map<String, Object>>) ((Map) response.get("realtimeArrivalList")).get("row");

               if (rows != null && !rows.isEmpty()) {
                Object trainStatus = rows.get(0).get("trainLineNm");
                // 혼잡도 파싱 로직 (API 응답 구조에 맞게 조정 필요)
                return parseCongestionRate(rows.get(0));
            }

        } catch (Exception e) {
            log.error("서울 지하철 혼잡도 API 호출 실패: {}", e.getMessage());
        }

          return estimateCongestion();
    }


    // 기상청 날씨 api 호출
    public String fetchWeather() {
        try {
            return "맑음";
        } catch (Exception e) {
            log.error("기상청 API 호출 실패: {}", e.getMessage());
            return "알 수 없음";
        
        }
    }


    private int parseCongestionRate(Map<String, Object> apiResponse) {
        // API 응답에서 혼잡도 정보를 추출하는 로직 구현
        // 예시: apiResponse.get("congestionRate") 등
        return 0; // 실제 혼잡도 값으로 대체
    
    
    }

    private int estimateCongestion() {
        // API 호출 실패 시 혼잡도를 추정하는 로직 구현
        // 예시: 과거 데이터 기반 평균값 반환 등
        return 50; // 예시로 50% 혼잡도 반환
    }

    
}
