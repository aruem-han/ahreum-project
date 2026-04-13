package com.congestion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.congestion.dto.Station;
import com.congestion.repository.StationRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final PublicApiService publicApiService;

    // API를 통해 불러온 전체 지하철역을 담아둘 메모리 캐시
    private List<Station> cachedStations = new ArrayList<>();

    @PostConstruct
    public void initStations() {
        log.info("서버 시작: 공공 API로 지하철역 마스터 정보 호출 및 캐싱 시작...");
        cachedStations = publicApiService.fetchAllStations();
        
        if (cachedStations.isEmpty()) {
            log.warn("지하철 마스터 API 응답 실패. 기본 폴백 데이터를 사용합니다.");
            cachedStations = List.of(
                Station.builder().id(1).name("강남").line("2호선").direction("내선").build(),
                Station.builder().id(2).name("고속터미널").line("9호선").direction("상행").build(),
                Station.builder().id(3).name("홍대입구").line("2호선").direction("외선").build(),
                Station.builder().id(4).name("잠실").line("2호선").direction("내선").build()
            );
        } else {
            log.info("지하철역 캐싱 완료: 총 {}개 역 로드됨", cachedStations.size());
        }
    }

    /**
     * 혼잡도를 즉시 조회하지 않고 기본 역 정보만 반환하는 헬퍼.
     * 프론트엔드에서 비동기로 혼잡도를 별도 조회하도록 rate를 -1로 설정한다.
     */
    private Station withBasicInfo(Station s, boolean isFavorite) {
        return new Station(
                s.getId(),
                s.getName(),
                s.getLine(),
                s.getDirection(),
                -1, // 로딩 상태
                isFavorite
        );
    }

    /**
     * DB 오류 발생 시에도 앱이 터지지 않도록 안전하게 즐겨찾기 목록을 가져옵니다.
     */
    private List<Integer> getSafeFavoriteIds() {
        try {
            return stationRepository.findFavoriteStationIds();
        } catch (Exception e) {
            log.error("DB 연동 오류 - 즐겨찾기 목록 조회 실패: {}", e.getMessage());
            return List.of(); // 오류 시 빈 리스트로 폴백하여 앱 정상 동작 보장
        }
    }

    /**
     * 역명 키워드 검색.
     * 캐시된 데이터에서 부분 일치 역을 찾은 뒤 혼잡도 -1 상태로 즉시 반환한다.
     *
     * @param keyword 검색어 (예: "강남", "홍대")
     * @return 검색된 역 목록 (최신 혼잡도 포함), 없으면 빈 리스트
     */
    public List<Station> searchStations(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();

        // 사용자가 "강남역"이라고 입력해도 "강남"으로 치환하여 정확도 향상
        String normalizedKeyword = keyword.trim().replaceAll("역$", "");
        log.info("🔍 검색 요청 수신: 원본='{}', 변환='{}'", keyword, normalizedKeyword);
        log.info("📦 현재 메모리에 캐싱된 역 개수: {}개", cachedStations.size());

        // 즐겨찾기 ID 목록 조회
        List<Integer> favoriteIds = getSafeFavoriteIds();

        List<Station> stations = cachedStations.stream()
                .filter(s -> s.getName() != null && s.getName().contains(normalizedKeyword))
                .limit(10) // 검색결과가 너무 많아 실시간 API 호출 병목이 생기는 것을 방지
                .map(s -> withBasicInfo(s, favoriteIds.contains(s.getId())))
                .toList();

        if (stations.isEmpty()) {
            log.warn("❌ 검색 결과 없음: 검색어='{}'", normalizedKeyword);
            if (!cachedStations.isEmpty()) {
                log.info("💡 (참고) 캐시된 역 샘플 확인: {}, {}, {}", 
                    cachedStations.get(0).getName(), 
                    cachedStations.size() > 1 ? cachedStations.get(1).getName() : "",
                    cachedStations.size() > 2 ? cachedStations.get(2).getName() : "");
            }
        } else {
            log.info("✅ 검색 성공: {}개의 역을 찾았습니다.", stations.size());
        }
        return stations;
    }

    /**
     * 즐겨찾기 역 목록 조회.
     * 즐겨찾기 역들을 반환하며, 혼잡도는 조회 대기(-1) 상태로 넘긴다.
     *
     * @return 즐겨찾기 역 목록 (최신 혼잡도 포함)
     */
    public List<Station> getFavorites() {
        List<Integer> favoriteIds = getSafeFavoriteIds();

        return cachedStations.stream()
                .filter(s -> favoriteIds.contains(s.getId()))
                .map(s -> withBasicInfo(s, true))
                .toList();
    }

    /**
     * 역 ID로 단건 혼잡도 조회.
     * 해당 역이 없으면 Optional.empty()를 반환하므로 컨트롤러에서 404 처리 가능.
     *
     * @param stationId 조회할 역 ID
     * @return 최신 혼잡도가 반영된 역 정보 (Optional)
     */
    public Optional<Station> getCongestionById(int stationId) {
        List<Integer> favoriteIds = getSafeFavoriteIds();

        return cachedStations.stream()
                .filter(s -> s.getId() == stationId)
                .findFirst()
                .map(s -> withLiveCongestion(s, favoriteIds.contains(s.getId())));
    }

    /**
     * 역 이름으로 상세 정보 조회 — 역 기본 정보 + 공공 API 도착 데이터 조합.
     * 프론트가 도착 열차 목록을 직접 렌더링해야 할 때 사용한다.
     *
     * @param stationName 정확한 역명 (예: "강남")
     * @return { "station": Station, "congestion": List<Map> } 형태의 맵 리스트
     */
    public List<Map<String, Object>> getStationInfo(String stationName) {
        if (stationName == null || stationName.isEmpty()) return List.of();

        List<Integer> favoriteIds = getSafeFavoriteIds();

        List<Station> stations = cachedStations.stream()
                .filter(s -> s.getName().equals(stationName)) // 정확히 일치
                .toList();

        if (stations.isEmpty()) {
            log.info("역 정보가 없습니다: {}", stationName);
            return List.of();
        }

        return stations.stream()
                .map(s -> {
                    Station updatedStation = withLiveCongestion(s, favoriteIds.contains(s.getId()));
                    List<Map<String, Object>> arrivalInfo = publicApiService.fetchSubwayCongestion(s.getName());
                    return Map.of(
                            "station", updatedStation,
                            "congestion", arrivalInfo
                    );
                })
                .toList();
    }
    private Station withLiveCongestion(Station s, boolean isFavorite) {
        int rate = publicApiService.estimateCongestion(s.getName());
        return new Station(
                s.getId(),
                s.getName(),
                s.getLine(),
                s.getDirection(),
                rate,
                isFavorite
        );
    }

    /**
     * 즐겨찾기 역 추가
     *
     * @param stationId 역 ID
     */
    public void addFavorite(int stationId) {
        stationRepository.addFavorite(stationId);
    }

    /**
     * 즐겨찾기 역 삭제
     *
     * @param stationId 역 ID
     */
    public void removeFavorite(int stationId) {
        stationRepository.removeFavorite(stationId);
    }
}
