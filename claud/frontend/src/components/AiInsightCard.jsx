export default function AiInsightCard({ message }) {
  return (
    <div style={styles.card}>
      <div style={styles.label}>AI 인사이트</div>
      <p style={styles.text}>{message}</p>
    </div>
  )
}

const styles = {
  card: {
    background: '#EEF4FF', border: '1px solid #B5D4F4',
    borderRadius: 14, padding: '14px 16px', marginTop: 4
  },
  label: { fontSize: 11, fontWeight: 700, color: '#185FA5', marginBottom: 6, letterSpacing: '0.3px' },
  text: { fontSize: 14, color: '#0C447C', lineHeight: 1.6 }
}