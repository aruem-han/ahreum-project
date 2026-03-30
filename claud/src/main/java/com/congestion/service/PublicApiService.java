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

    private static final String BASE_URL =
        "http://swopenapi.seoul.go.kr/api/subway/%s/json/realtimeStationArrival/0/10/%s";

        public List<Map<String, Object>> fetchSubwayCongestion(String stationName) {
            String url = String.format(BASE_URL, seoulKey, stationName);

        try{
            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null) return List.of();

            //에러 
            Map errorMsg = (Map) response.get("errorMessage");
            if(errorMsg!=null) {
                String code = (String) errorMsg.get("code");
                    if (!"INFO-000".equals(code)) {
                    log.warn("API 오류: {}", errorMsg.get("message"));
                    return List.of();
                }
            }

            List<Map<String, Object>> arrivals = (List<Map<String, Object>>) response.get("realtimeArrivalList");
            return arrivals != null ? arrivals : List.of();
           
        } catch (Exception e) {
            log.error("서울 지하철 혼잡도 API 호출 실패: {}", e.getMessage());
            return List.of();
        }
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

    public int estimateCongestion(String stationName) {
        List<Map<String, Object>> arrivals = fetchSubwayCongestion(stationName);
        
        if (arrivals.isEmpty()) {
            return estimateByTime();  // API 실패 시 시간대 기반 추정
        }

        long arrivingSoon = arrivals.stream()
            .filter(a -> {
                Object barvlDt = a.get("barvlDt");
                if (barvlDt == null) return false;
                int seconds = Integer.parseInt(barvlDt.toString());
                return seconds <= 300; // 5분 이내 도착
            })
            .count();

        int hour = java.time.LocalTime.now().getHour();
        boolean isPeak = (hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 20);

        if (isPeak && arrivingSoon >= 2) return 85;
        if (isPeak && arrivingSoon == 1) return 70;
        if (!isPeak && arrivingSoon >= 2) return 45;
        if (!isPeak && arrivingSoon == 1) return 30;
        return estimateByTime();
    }
    
     // API 실패 시 시간대 기반 추정
    public int estimateByTime() {
        int hour = java.time.LocalTime.now().getHour();
        if ((hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 20)) return 80;
        if (hour >= 10 && hour <= 17) return 40;
        return 25;
    }
    
}
