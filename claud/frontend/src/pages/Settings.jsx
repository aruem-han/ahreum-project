export default function Settings() {
  return (
    <div className="page">
      <div style={styles.header}>
        <h2 style={styles.title}>설정</h2>
      </div>
      <div style={{ padding: 16 }}>
        <div style={styles.section}>
          <p style={styles.sectionTitle}>즐겨찾기 관리</p>
          <div style={styles.item}>
            <span>강남역 (2호선)</span>
            <button style={styles.removeBtn}>삭제</button>
          </div>
          <div style={styles.item}>
            <span>고속터미널역 (9호선)</span>
            <button style={styles.removeBtn}>삭제</button>
          </div>
        </div>
        <div style={styles.section}>
          <p style={styles.sectionTitle}>알림</p>
          <div style={styles.item}>
            <span>혼잡도 알림</span>
            <input type="checkbox" defaultChecked />
          </div>
        </div>
      </div>
    </div>
  )
}

const styles = {
  header: { background: '#1A56C4', padding: '28px 20px 24px' },
  title: { color: '#fff', fontSize: 20, fontWeight: 600 },
  section: { background: '#fff', borderRadius: 12, padding: 16, marginBottom: 14, border: '1px solid #eee' },
  sectionTitle: { fontSize: 12, fontWeight: 600, color: '#888', marginBottom: 12, textTransform: 'uppercase' },
  item: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0', borderBottom: '1px solid #f5f5f5', fontSize: 14 },
  removeBtn: { fontSize: 12, color: '#E24B4A', background: 'none', border: 'none', cursor: 'pointer' }
}