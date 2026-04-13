import { useState, useEffect } from 'react'
import { getCongestion, addFavorite, removeFavorite } from '../api/congestion'

const LINE_COLORS = {
  '1호선': '#0052A4', '2호선': '#00A84D', '3호선': '#EF7C1C',
  '4호선': '#00A5DE', '5호선': '#996CAC', '6호선': '#CD7C2F',
  '7호선': '#747F00', '8호선': '#E6186C', '9호선': '#BDB092',
}

const LEVEL_MAP = {
  low:  { label: '여유',     color: '#3B6D11', bg: '#EAF3DE' },
  mid:  { label: '보통',     color: '#633806', bg: '#FAEEDA' },
  high: { label: '혼잡',     color: '#A32D2D', bg: '#FCEBEB' },
  full: { label: '매우 혼잡', color: '#791F1F', bg: '#F7C1C1' },
  loading: { label: '조회 중...', color: '#666', bg: '#eee' },
}

function getLevel(rate) {
  if (rate < 0) return 'loading' // -1일 경우 로딩 상태 반환
  if (rate < 40) return 'low'
  if (rate < 65) return 'mid'
  if (rate < 85) return 'high'
  return 'full'
}

export default function StationCard({ station }) {
  const [liveData, setLiveData] = useState(station)
  const [isLoading, setIsLoading] = useState(station.congestionRate < 0)
  
  // Spring Boot(Jackson) 설정에 따라 favorite 또는 isFavorite으로 내려올 수 있으므로 방어적 할당
  const [isFav, setIsFav] = useState(station.favorite ?? station.isFavorite ?? false)

  // 카드가 렌더링될 때 혼잡도(-1)면 API 호출
  useEffect(() => {
    let isMounted = true
    setLiveData(station)

    const fetchWithRetry = async (retries = 3) => {
      for (let i = 0; i < retries; i++) {
        try {
          const data = await getCongestion(station.id)
          if (isMounted) setLiveData(data)
          return
        } catch (e) {
          if (i === retries - 1) {
            console.error(`혼잡도 조회 최종 실패 (역 ID: ${station.id})`, e)
          } else {
            // 동시 요청 분산을 위해 랜덤 지연(Jitter) 추가: 0.5초 ~ 1.5초 대기
            await new Promise(resolve => setTimeout(resolve, 500 + Math.random() * 1000))
          }
        }
      }
    }

    if (station.congestionRate < 0) {
      setIsLoading(true)
      fetchWithRetry().finally(() => isMounted && setIsLoading(false))
    } else {
      setIsLoading(false)
    }

    return () => { isMounted = false }
  }, [station])

  // 별 버튼 클릭 시 즐겨찾기 토글
  const toggleFavorite = async (e) => {
    e.stopPropagation() // 클릭 이벤트가 카드 부모로 전파되는 것 방지
    try {
      if (isFav) {
        await removeFavorite(station.id)
        setIsFav(false)
      } else {
        await addFavorite(station.id)
        setIsFav(true)
      }
    } catch (err) {
      console.error(err)
      alert('즐겨찾기 변경에 실패했습니다.')
    }
  }

  const level = getLevel(liveData.congestionRate)
  const { label, color, bg } = LEVEL_MAP[level]
  const lineColor = LINE_COLORS[liveData.line] || '#888'

  return (
    <div style={styles.card}>
      <div style={{ ...styles.badge, background: lineColor }}>
        {liveData.line?.replace('호선', '')}
      </div>
      <div style={{ flex: 1 }}>
        <div style={styles.row}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={styles.name}>{liveData.name}</span>
            <span style={{ ...styles.levelBadge, color, background: bg }}>{label}</span>
          </div>
          <button 
            onClick={toggleFavorite}
            style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: 24, color: isFav ? '#FFD700' : '#ddd', padding: 0 }}
          >
            {isFav ? '★' : '☆'}
          </button>
        </div>
        <div style={styles.meta}>{liveData.line} · {isLoading ? '실시간 데이터 불러오는 중...' : '방금 업데이트'}</div>
        <CongestionBar rate={liveData.congestionRate} level={level} isLoading={isLoading} />
      </div>
    </div>
  )
}

function CongestionBar({ rate, level, isLoading }) {
  const COLOR = { low: '#2DB400', mid: '#EF9F27', high: '#E24B4A', full: '#A32D2D', loading: '#cccccc' }
  const filled = isLoading ? 0 : Math.round(rate / 20)

  return (
    <div style={{ display: 'flex', gap: 3, marginTop: 8 }}>
      {Array.from({ length: 5 }, (_, i) => (
        <div key={i} style={{
          flex: 1, height: 4, borderRadius: 2,
          background: isLoading ? '#eee' : (i < filled ? COLOR[level] : '#e5e5e5')
        }} />
      ))}
    </div>
  )
}

const styles = {
  card: {
    background: '#fff', borderRadius: 14, padding: '14px 16px',
    marginBottom: 10, display: 'flex', alignItems: 'center',
    gap: 14, border: '1px solid #eee'
  },
  badge: {
    width: 32, height: 32, borderRadius: '50%', flexShrink: 0,
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: 12, fontWeight: 600, color: '#fff'
  },
  row: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  name: { fontSize: 15, fontWeight: 600 },
  levelBadge: {
    fontSize: 11, fontWeight: 600, padding: '3px 8px',
    borderRadius: 20
  },
  meta: { fontSize: 11, color: '#aaa', marginTop: 3 }
}