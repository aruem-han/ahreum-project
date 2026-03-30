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
}

function getLevel(rate) {
  if (rate < 40) return 'low'
  if (rate < 65) return 'mid'
  if (rate < 85) return 'high'
  return 'full'
}

export default function StationCard({ station }) {
  const level = getLevel(station.congestionRate)
  const { label, color, bg } = LEVEL_MAP[level]
  const lineColor = LINE_COLORS[station.line] || '#888'

  return (
    <div style={styles.card}>
      <div style={{ ...styles.badge, background: lineColor }}>
        {station.line?.replace('호선', '')}
      </div>
      <div style={{ flex: 1 }}>
        <div style={styles.row}>
          <span style={styles.name}>{station.name}</span>
          <span style={{ ...styles.levelBadge, color, background: bg }}>{label}</span>
        </div>
        <div style={styles.meta}>{station.line} · 방금 업데이트</div>
        <CongestionBar rate={station.congestionRate} level={level} />
      </div>
    </div>
  )
}

function CongestionBar({ rate, level }) {
  const COLOR = { low: '#2DB400', mid: '#EF9F27', high: '#E24B4A', full: '#A32D2D' }
  const filled = Math.round(rate / 20)

  return (
    <div style={{ display: 'flex', gap: 3, marginTop: 8 }}>
      {Array.from({ length: 5 }, (_, i) => (
        <div key={i} style={{
          flex: 1, height: 4, borderRadius: 2,
          background: i < filled ? COLOR[level] : '#e5e5e5'
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