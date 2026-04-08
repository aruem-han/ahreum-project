package com.congestion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.congestion.dto.Station;

/**
 * 지하철역 정보 저장소.
 *
 * 현재는 DB 없이 동작하도록 서울 주요 노선의 인메모리 데이터를 사용한다.
 * 향후 MyBatis 매퍼(StationMapper.xml)와 연동 시 이 클래스를 교체하면 된다.
 */
@Repository
public class StationRepository {

    /**
     * 서울 주요 지하철역 기본 데이터.
     * congestionRate는 초기값이며, 실제 값은 PublicApiService.estimateCongestion()으로 덮어씌워진다.
     */
    private static final List<Station> STATIONS = List.of(
        Station.builder().id(1).name("강남").line("2호선").direction("내선").congestionRate(80).build(),
        Station.builder().id(2).name("고속터미널").line("9호선").direction("상행").congestionRate(45).build(),
        Station.builder().id(3).name("홍대입구").line("2호선").direction("외선").congestionRate(25).build(),
        Station.builder().id(4).name("잠실").line("2호선").direction("내선").congestionRate(65).build(),
        Station.builder().id(5).name("서울역").line("1호선").direction("하행").congestionRate(55).build(),
        Station.builder().id(6).name("신촌").line("2호선").direction("내선").congestionRate(30).build(),
        Station.builder().id(7).name("종로3가").line("3호선").direction("하행").congestionRate(40).build(),
        Station.builder().id(8).name("사당").line("4호선").direction("하행").congestionRate(70).build(),
        Station.builder().id(9).name("왕십리").line("5호선").direction("하행").congestionRate(50).build(),
        Station.builder().id(10).name("이태원").line("6호선").direction("하행").congestionRate(35).build(),
        Station.builder().id(11).name("건대입구").line("7호선").direction("상행").congestionRate(60).build(),
        Station.builder().id(12).name("잠실나루").line("2호선").direction("내선").congestionRate(42).build(),
        Station.builder().id(13).name("신림").line("2호선").direction("외선").congestionRate(55).build(),
        Station.builder().id(14).name("여의도").line("9호선").direction("상행").congestionRate(48).build(),
        Station.builder().id(15).name("광화문").line("5호선").direction("하행").congestionRate(38).build()
    );

    /**
     * 역명 부분 일치 검색.
     * 예: "강남" → "강남", "강남구청" 등 이름에 "강남"이 포함된 역 모두 반환.
     */
    public List<Station> findByName(String name) {
        return STATIONS.stream()
                .filter(s -> s.getName().contains(name))
                .toList();
    }

    /**
     * 즐겨찾기 역 목록 반환 (고정 4개).
     * 추후 사용자별 즐겨찾기 DB 테이블 연동 시 이 메서드를 교체한다.
     */
    public List<Station> findFavorites() {
        // 강남, 고속터미널, 홍대입구, 잠실 고정
        return STATIONS.subList(0, 4);
    }

    /**
     * ID로 단건 조회.
     * 존재하지 않으면 Optional.empty() 반환.
     */
    public Optional<Station> findById(int id) {
        return STATIONS.stream()
                .filter(s -> s.getId() == id)
                .findFirst();
    }
}
