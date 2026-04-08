package com.congestion.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.congestion.dto.Station;
import com.congestion.repository.StationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 지하철역 비즈니스 로직 서비스.
 *
 * StationRepository의 기본 역 정보에 PublicApiService의 실시간 혼잡도를 합산하여
 * 최신 상태를 반환한다. 모든 공개 메서드는 새로운 Station 객체를 빌드해 불변성을 유지한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;
    private final PublicApiService publicApiService;

    /**
     * 역명 키워드 검색.
     * 인메모리 데이터에서 부분 일치 역을 찾은 뒤, 각 역의 혼잡도를
     * PublicApiService.estimateCongestion()으로 실시간 갱신한다.
     *
     * @param keyword 검색어 (예: "강남", "홍대")
     * @return 검색된 역 목록 (최신 혼잡도 포함), 없으면 빈 리스트
     */
    public List<Station> searchStations(String keyword) {
        if (keyword == null || keyword.isBlank()) return List.of();

        List<Station> stations = stationRepository.findByName(keyword);
        if (stations.isEmpty()) {
            log.info("검색 결과 없음: {}", keyword);
            return List.of();
        }

        // 공공 API로 실시간 혼잡도 갱신
        return stations.stream()
                .map(s -> withLiveCongestion(s))
                .toList();
    }

    /**
     * 즐겨찾기 역 목록 조회.
     * 고정 4개 역을 반환하며, 각 역의 혼잡도를 실시간으로 업데이트한다.
     *
     * @return 즐겨찾기 역 목록 (최신 혼잡도 포함)
     */
    public List<Station> getFavorites() {
        return stationRepository.findFavorites().stream()
                .map(s -> withLiveCongestion(s))
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
        return stationRepository.findById(stationId)
                .map(s -> withLiveCongestion(s));
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

        List<Station> stations = stationRepository.findByName(stationName);
        if (stations.isEmpty()) {
            log.info("역 정보가 없습니다: {}", stationName);
            return List.of();
        }

        return stations.stream()
                .map(s -> {
                    List<Map<String, Object>> arrivalInfo = publicApiService.fetchSubwayCongestion(s.getName());
                    return Map.of(
                            "station", s,
                            "congestion", arrivalInfo
                    );
                })
                .toList();
    }

    /**
     * 공통 헬퍼: 기존 Station 객체에 실시간 혼잡도를 적용해 새 Station 반환.
     * PublicApiService 호출이 실패해도 estimateByTime()으로 시간대 기반 값이 반환된다.
     */
    private Station withLiveCongestion(Station s) {
        int rate = publicApiService.estimateCongestion(s.getName());
        return Station.builder()
                .id(s.getId())
                .name(s.getName())
                .line(s.getLine())
                .direction(s.getDirection())
                .congestionRate(rate)
                .build();
    }
}
