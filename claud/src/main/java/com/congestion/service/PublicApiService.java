package com.congestion.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.congestion.dto.Station;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicApiService {

    private final RestTemplate restTemplate;
    @Value("${app.public-api.station-key}")
    private String stationApiKey;

    @Value("${app.public-api.tago-key}")
    private String weatherKey;

    // 기상청 초단기실황 조회 API (서울 중구 기준 nx=60, ny=127)
    private static final String WEATHER_URL = 
        "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst?serviceKey=%s&pageNo=1&numOfRows=10&dataType=JSON&base_date=%s&base_time=%s&nx=60&ny=127";

    // 서울시 지하철역 마스터 정보 API
    private static final String MASTER_URL = "http://openapi.seoul.go.kr:8088/%s/json/subwayStationMaster/1/999/";

    //서울 지하철 실시간 혼잡도 api호출

    private static final String BASE_URL =
        "http://swopenapi.seoul.go.kr/api/subway/%s/json/realtimeStationArrival/0/10/%s";

        public List<Map<String, Object>> fetchSubwayCongestion(String stationName) {
            String url = String.format(BASE_URL, stationApiKey, stationName);

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

    // 서버 시작 시 지하철역 마스터 정보 목록 캐싱용 호출
    public List<Station> fetchAllStations() {
        String url = String.format(MASTER_URL, stationApiKey);
        try {
            Map response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("subwayStationMaster")) {
                Map master = (Map) response.get("subwayStationMaster");
                List<Map<String, Object>> rows = (List<Map<String, Object>>) master.get("row");
                
                if (rows != null && !rows.isEmpty()) {
                    // 🚨 확실한 추적을 위해 공공 API가 준 '첫 번째 데이터 원본 전체'를 강제로 출력합니다.
                    log.error("🚨 [긴급 디버깅] 공공 API 데이터 원본: {}", rows.get(0));

                    return rows.stream().map(row -> {
                        String name = "에러:이름없음";
                        String line = "에러:호선없음";

                        // 어떤 형태의 키값(대소문자, 언더바)이 들어와도 무조건 찾아내는 궁극의 로직
                        for (Object keyObj : row.keySet()) {
                            if (keyObj == null) continue;
                            String key = keyObj.toString().toUpperCase().replace("_", "").replace("-", "");
                            
                            if (key.equals("STATNNM") || key.equals("STATIONNM") || key.equals("BLDNNM") || key.equals("NAME")) {
                                name = String.valueOf(row.get(keyObj)).trim();
                            }
                            if (key.equals("ROUTE") || key.equals("ROUTENM") || key.equals("LINENUM") || key.equals("LINE")) {
                                line = String.valueOf(row.get(keyObj)).trim();
                            }
                        }

                        // ID가 문자(예: 수인분당선 K210)일 수 있으므로 역명+호선 조합의 안정적인 해시코드를 int ID로 사용
                        int id = Math.abs((name + line).hashCode());

                        // 롬복(Lombok) @Builder 컴파일 꼬임 방지를 위해 명시적 생성자 사용
                        return new Station(id, name, line, "상하행", 0, false);
                    }).toList();
                }
            } else {
                log.warn("지하철 마스터 API 응답 오류 (키 미활성화 또는 오류): {}", response);
            }
        } catch (Exception e) {
            log.error("지하철역 마스터 정보 API 호출 실패: {}", e.getMessage());
        }
        return List.of();
    }

    // 기상청 날씨 api 호출
    public String fetchWeather() {
        try {
            LocalDateTime now = LocalDateTime.now();
            // 기상청 초단기실황 API는 매시간 40분에 발표되므로, 40분 이전이면 이전 시간으로 조회
            if (now.getMinute() < 40) {
                now = now.minusHours(1);
            }
            String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String baseTime = now.format(DateTimeFormatter.ofPattern("HH00"));

            String urlString = String.format(WEATHER_URL, weatherKey, baseDate, baseTime);
            // 기상청 서비스 키(ServiceKey)의 특수문자 이중 인코딩 방지를 위해 URI 객체 사용
            URI uri = new URI(urlString);
            Map response = restTemplate.getForObject(uri, Map.class);
            
            if (response != null && response.containsKey("response")) {
                Map responseNode = (Map) response.get("response");
                Map body = (Map) responseNode.get("body");
                if (body != null && body.containsKey("items")) {
                    Map items = (Map) body.get("items");
                    List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");
                    
                    if (itemList != null) {
                        for (Map<String, Object> item : itemList) {
                            if ("PTY".equals(item.get("category"))) { // PTY: 강수형태
                                String obsrValue = String.valueOf(item.get("obsrValue"));
                                return switch (obsrValue) {
                                    case "1", "4", "5" -> "비";
                                    case "2", "6" -> "비/눈";
                                    case "3", "7" -> "눈";
                                    default -> "맑음";
                                };
                            }
                        }
                    }
                }
            }
            return "맑음"; // 데이터 파싱 실패 또는 조건에 맞지 않을 시 폴백 처리
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
