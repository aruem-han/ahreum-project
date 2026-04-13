-- 기존 테이블이 있다면 삭제 (H2 등 인메모리 DB를 사용한 로컬 개발용)
DROP TABLE IF EXISTS station;
DROP TABLE IF EXISTS user_favorite;

-- 공용 즐겨찾기 테이블 (로그인 정보 제외)
CREATE TABLE user_favorite (
    id BIGSERIAL PRIMARY KEY,
    station_id INT NOT NULL
);
