import { useState } from 'react'
import StationCard from '../components/StationCard'
import { searchStations } from '../api/congestion'

export default function Search() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [searched, setSearched] = useState(false)

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!query.trim()) return
    const data = await searchStations(query)
    setResults(data)
    setSearched(true)
  }

  return (
    <div className="page">
      <div style={styles.header}>
        <h2 style={styles.title}>역 검색</h2>
        <form onSubmit={handleSearch} style={styles.form}>
          <input
            style={styles.input}
            value={query}
            onChange={e => setQuery(e.target.value)}
            placeholder="역 이름을 입력하세요"
            autoFocus
          />
          <button type="submit" style={styles.btn}>검색</button>
        </form>
      </div>

      <div style={{ padding: '16px' }}>
        {searched && results.length === 0 && (
          <p style={styles.empty}>검색 결과가 없어요</p>
        )}
        {results.map(station => (
          <StationCard key={station.id} station={station} />
        ))}
      </div>
    </div>
  )
}

const styles = {
  header: { background: '#1A56C4', padding: '28px 16px 20px' },
  title: { color: '#fff', fontSize: 20, fontWeight: 600, marginBottom: 14 },
  form: { display: 'flex', gap: 8 },
  input: {
    flex: 1, padding: '10px 14px', borderRadius: 10,
    border: 'none', fontSize: 15, outline: 'none'
  },
  btn: {
    padding: '10px 16px', borderRadius: 10, border: 'none',
    background: 'rgba(255,255,255,0.25)', color: '#fff',
    fontSize: 14, cursor: 'pointer', fontWeight: 500
  },
  empty: { textAlign: 'center', color: '#aaa', fontSize: 14, padding: 32 }
}