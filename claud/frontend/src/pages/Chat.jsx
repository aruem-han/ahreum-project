import { useState, useRef, useEffect } from 'react'
import { getAiInsight } from '../api/congestion'

const SUGGESTIONS = [
  '지금 2호선 강남역 어때?',
  '퇴근길에 덜 붐비는 노선 추천해줘',
  '오늘 날씨 때문에 더 붐비나?',
]

export default function Chat() {
  const [messages, setMessages] = useState([
    { role: 'ai', text: '안녕하세요! 대중교통 혼잡도에 대해 무엇이든 물어보세요.' }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const send = async (query) => {
    const q = query || input.trim()
    if (!q || loading) return
    setInput('')
    setMessages(prev => [...prev, { role: 'user', text: q }])
    setLoading(true)

    try {
      const data = await getAiInsight({ userQuery: q })
      setMessages(prev => [...prev, { role: 'ai', text: data }])
    } catch {
      setMessages(prev => [...prev, { role: 'ai', text: '잠시 후 다시 시도해 주세요.' }])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page" style={{ display: 'flex', flexDirection: 'column' }}>
      <div style={styles.header}>
        <h2 style={styles.title}>AI 채팅</h2>
        <p style={styles.sub}>대중교통 혼잡도 무엇이든 물어보세요</p>
      </div>

      <div style={styles.messages}>
        {messages.map((m, i) => (
          <div key={i} style={{ display: 'flex', justifyContent: m.role === 'user' ? 'flex-end' : 'flex-start', marginBottom: 12 }}>
            <div style={m.role === 'user' ? styles.userBubble : styles.aiBubble}>
              {m.text}
            </div>
          </div>
        ))}
        {loading && (
          <div style={{ display: 'flex', justifyContent: 'flex-start', marginBottom: 12 }}>
            <div style={styles.aiBubble}>분석 중...</div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div style={styles.suggestions}>
        {SUGGESTIONS.map((s, i) => (
          <button key={i} style={styles.suggBtn} onClick={() => send(s)}>{s}</button>
        ))}
      </div>

      <div style={styles.inputRow}>
        <input
          style={styles.input}
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && send()}
          placeholder="질문을 입력하세요"
        />
        <button style={styles.sendBtn} onClick={() => send()}>전송</button>
      </div>
    </div>
  )
}

const styles = {
  header: { background: '#1A56C4', padding: '28px 20px 20px', color: '#fff' },
  title: { fontSize: 20, fontWeight: 600, marginBottom: 4 },
  sub: { fontSize: 13, opacity: 0.75 },
  messages: { flex: 1, padding: '16px', overflowY: 'auto' },
  userBubble: {
    background: '#1A56C4', color: '#fff', borderRadius: '18px 18px 4px 18px',
    padding: '10px 14px', fontSize: 14, maxWidth: '75%', lineHeight: 1.5
  },
  aiBubble: {
    background: '#fff', color: '#1a1a1a', borderRadius: '18px 18px 18px 4px',
    padding: '10px 14px', fontSize: 14, maxWidth: '75%', lineHeight: 1.5,
    border: '1px solid #eee'
  },
  suggestions: { display: 'flex', gap: 8, padding: '0 16px 10px', flexWrap: 'wrap' },
  suggBtn: {
    fontSize: 12, padding: '6px 12px', borderRadius: 20,
    border: '1px solid #1A56C4', background: '#EEF4FF',
    color: '#1A56C4', cursor: 'pointer', whiteSpace: 'nowrap'
  },
  inputRow: {
    display: 'flex', gap: 8, padding: '10px 16px 16px',
    background: '#fff', borderTop: '1px solid #eee'
  },
  input: {
    flex: 1, padding: '10px 14px', borderRadius: 22,
    border: '1px solid #ddd', fontSize: 14, outline: 'none'
  },
  sendBtn: {
    padding: '10px 18px', borderRadius: 22, border: 'none',
    background: '#1A56C4', color: '#fff', fontSize: 14,
    fontWeight: 500, cursor: 'pointer'
  }
}