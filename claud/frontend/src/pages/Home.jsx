import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import StationCard from '../components/StationCard'
import AiInsightCard from '../components/AiInsightCard'
import { getFavorites, getAiInsight } from '../api/congestion'

const DUMMY_STATIONS = [
  { id: 1, name: '강남', line: '2호선', congestionRate: 85, direction: '내선' },
  { id: 2, name: '고속터미널', line: '9호선', congestionRate: 45, direction: '상행' },
  { id: 3, name: '홍대입구', line: '2호선', congestionRate: 25, direction: '외선' },
  { id: 4, name: '잠실', line: '2호선', congestionRate: 65, direction: '내선' },
]

const DUMMY_INSIGHT = '지금 강남역은 퇴근 피크 시간대라 꽤 혼잡해요. 19시 이후에 출발하면 혼잡도가 절반 수준으로 줄어들 거예요. 가능하면 1번 또는 마지막 칸을 이용해보세요.'

export default function Home() {
  const navigate = useNavigate()
  const [stations, setStations] = useState(DUMMY_STATIONS)
  const [insight, setInsight] = useState(DUMMY_INSIGHT)
  const [loading, setLoading] = useState(false)
  const [time, setTime] = useState(new Date())

  // 실시간 시계
  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000)
    return () => clearInterval(timer)
  }, [])

  // 마운트 시 백엔드 API에서 즐겨찾기 역 목록 + AI 인사이트를 가져온다.
  // 백엔드가 응답하지 않으면 더미 데이터로 폴백해 화면이 빈 채로 남지 않도록 처리한다.
  useEffect(() => {
    // 즐겨찾기 역 목록: GET /api/stations/favorites
    getFavorites()
      .then(data => {
        if (Array.isArray(data) && data.length > 0) setStations(data)
      })
      .catch(() => {
        // 백엔드 미실행 시 더미 데이터 유지
        setStations(DUMMY_STATIONS)
      })

    // AI 전반 인사이트: POST /api/ai/insight
    getAiInsight({ userQuery: '지금 서울 지하철 전반적인 혼잡 상황을 알려줘.' })
      .then(data => {
        // 백엔드가 plain string을 반환하므로 typeof 체크 후 사용
        const msg = typeof data === 'string' ? data : data?.message
        if (msg) setInsight(msg)
      })
      .catch(() => {
        setInsight(DUMMY_INSIGHT)
      })
  }, [])

  const formatTime = (date) => {
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit', minute: '2-digit', hour12: false
    })
  }

  const formatDate = (date) => {
    return date.toLocaleDateString('ko-KR', {
      month: 'long', day: 'numeric', weekday: 'short'
    })
  }

  return (
    <div style={styles.page}>
      {/* 헤더 */}
      <div style={styles.header}>
        <div style={styles.headerTop}>
          <div>
            <div style={styles.headerTitle}>지금 붐비나요?</div>
            <div style={styles.headerSub}>서울 대중교통 실시간 혼잡도</div>
          </div>
          <div style={styles.timeBox}>
            <div style={styles.timeText}>{formatTime(time)}</div>
            <div style={styles.dateText}>{formatDate(time)}</div>
          </div>
        </div>

        {/* 검색바 */}
        <div style={styles.searchBar} onClick={() => navigate('/search')}>
          <svg width="14" height="14" viewBox="0 0 16 16" fill="none">
            <circle cx="6.5" cy="6.5" r="4.5" stroke="rgba(255,255,255,0.7)" strokeWidth="1.5"/>
            <line x1="10" y1="10" x2="14" y2="14" stroke="rgba(255,255,255,0.7)" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
          <span style={styles.searchText}>역 이름으로 검색하세요</span>
        </div>
      </div>

      <div style={styles.content}>
        {/* 혼잡도 범례 */}
        <div style={styles.legend}>
          {[
            { label: '여유', color: '#2DB400' },
            { label: '보통', color: '#EF9F27' },
            { label: '혼잡', color: '#E24B4A' },
            { label: '매우 혼잡', color: '#A32D2D' },
          ].map(item => (
            <div key={item.label} style={styles.legendItem}>
              <div style={{ ...styles.legendDot, background: item.color }} />
              <span style={styles.legendLabel}>{item.label}</span>
            </div>
          ))}
        </div>

        {/* AI 인사이트 */}
        <AiInsightCard message={insight} />

        {/* 즐겨찾는 역 */}
        <div style={styles.sectionHeader}>
          <span style={styles.sectionTitle}>즐겨찾는 역</span>
          <span style={styles.sectionCount}>{stations.length}개</span>
        </div>

        {loading ? (
          <div style={styles.loadingWrap}>
            <div style={styles.loadingText}>불러오는 중...</div>
          </div>
        ) : stations.length === 0 ? (
          <div style={styles.emptyWrap}>
            <div style={styles.emptyText}>등록된 즐겨찾기가 없습니다.</div>
          </div>
        ) : (
          stations.map(station => (
            <StationCard key={station.id} station={station} />
          ))
        )}

        {/* 즐겨찾기 추가 버튼 */}
        <div style={styles.addBtn} onClick={() => navigate('/search')}>
          <span style={styles.addBtnIcon}>+</span>
          <span style={styles.addBtnText}>역 추가하기</span>
        </div>
      </div>
    </div>
  )
}

const styles = {
  page: {
    minHeight: '100vh',
    background: '#f5f6f8',
    paddingBottom: 80,
  },
  header: {
    background: '#1A56C4',
    padding: '48px 20px 20px',
  },
  headerTop: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 14,
  },
  headerTitle: {
    fontSize: 22,
    fontWeight: 700,
    color: '#fff',
    marginBottom: 4,
  },
  headerSub: {
    fontSize: 13,
    color: 'rgba(255,255,255,0.7)',
  },
  timeBox: {
    textAlign: 'right',
  },
  timeText: {
    fontSize: 20,
    fontWeight: 600,
    color: '#fff',
  },
  dateText: {
    fontSize: 11,
    color: 'rgba(255,255,255,0.65)',
    marginTop: 2,
  },
  searchBar: {
    background: 'rgba(255,255,255,0.15)',
    borderRadius: 10,
    padding: '10px 14px',
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    cursor: 'pointer',
  },
  searchText: {
    fontSize: 13,
    color: 'rgba(255,255,255,0.65)',
  },
  content: {
    padding: '16px 16px 0',
  },
  legend: {
    display: 'flex',
    gap: 14,
    marginBottom: 14,
    padding: '10px 14px',
    background: '#fff',
    borderRadius: 10,
    border: '1px solid #eee',
  },
  legendItem: {
    display: 'flex',
    alignItems: 'center',
    gap: 5,
  },
  legendDot: {
    width: 8,
    height: 8,
    borderRadius: '50%',
  },
  legendLabel: {
    fontSize: 11,
    color: '#666',
  },
  sectionHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
    marginTop: 4,
  },
  sectionTitle: {
    fontSize: 13,
    fontWeight: 700,
    color: '#444',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
  },
  sectionCount: {
    fontSize: 12,
    color: '#aaa',
  },
  loadingWrap: {
    textAlign: 'center',
    padding: 32,
  },
  loadingText: {
    fontSize: 14,
    color: '#aaa',
  },
  emptyWrap: {
    textAlign: 'center',
    padding: '40px 0',
  },
  emptyText: {
    fontSize: 14,
    color: '#888',
  },
  addBtn: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    padding: '14px',
    marginTop: 8,
    borderRadius: 14,
    border: '1.5px dashed #B5D4F4',
    background: '#EEF4FF',
    cursor: 'pointer',
  },
  addBtnIcon: {
    fontSize: 18,
    color: '#1A56C4',
    fontWeight: 300,
  },
  addBtnText: {
    fontSize: 14,
    color: '#1A56C4',
    fontWeight: 500,
  },
}