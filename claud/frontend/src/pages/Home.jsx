import { useState, useEffect } from 'react'
import StationCard from '../components/StationCard'
import AiInsightCard from '../components/AiInsightCard'
import { getFavorites, getAiInsight } from '../api/congestion'

export default function Home() {
  const [stations, setStations] = useState([])
  const [insight, setInsight] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getFavorites().then(data => {
      setStations(data)
      setLoading(false)
    })
    getAiInsight({ userQuery: '지금 전반적으로 어떤 상황이야?' })
      .then(data => setInsight(data.message))
  }, [])

  return (
    <div className="page">
      <div style={styles.header}>
        <h1 style={styles.title}>지금 붐비나요?</h1>
        <p style={styles.subtitle}>서울 대중교통 실시간 혼잡도</p>
      </div>

      <div style={styles.section}>
        <p style={styles.sectionTitle}>즐겨찾는 역</p>
        {loading ? (
          <p style={styles.loading}>불러오는 중...</p>
        ) : (
          stations.map(station => (
            <StationCard key={station.id} station={station} />
          ))
        )}
      </div>

      {insight && (
        <div style={{ padding: '0 16px' }}>
          <AiInsightCard message={insight} />
        </div>
      )}
    </div>
  )
}

const styles = {
  header: {
    background: '#1A56C4',
    padding: '28px 20px 24px',
    color: '#fff',
  },
  title: { fontSize: 22, fontWeight: 600, marginBottom: 4 },
  subtitle: { fontSize: 13, opacity: 0.75 },
  section: { padding: '16px 16px 0' },
  sectionTitle: { fontSize: 12, fontWeight: 600, color: '#888', marginBottom: 10, textTransform: 'uppercase', letterSpacing: '0.5px' },
  loading: { fontSize: 14, color: '#aaa', textAlign: 'center', padding: 24 }
}