import { useLocation, useNavigate } from 'react-router-dom'

const TABS = [
  { path: '/',         label: '홈',     icon: 'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z' },
  { path: '/search',  label: '검색',   icon: 'M21 21l-4.35-4.35M17 11A6 6 0 105 11a6 6 0 0012 0z' },
  { path: '/chat',    label: 'AI 채팅', icon: 'M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z' },
  { path: '/settings', label: '설정',  icon: 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z M15 12a3 3 0 11-6 0 3 3 0 016 0z' },
]

export default function BottomNav() {
  const { pathname } = useLocation()
  const navigate = useNavigate()

  return (
    <nav style={styles.nav}>
      {TABS.map(tab => {
        const active = pathname === tab.path
        return (
          <button key={tab.path} onClick={() => navigate(tab.path)} style={styles.btn}>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none"
              stroke={active ? '#1A56C4' : '#aaa'} strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round">
              <path d={tab.icon} />
            </svg>
            <span style={{ ...styles.label, color: active ? '#1A56C4' : '#aaa' }}>
              {tab.label}
            </span>
          </button>
        )
      })}
    </nav>
  )
}

const styles = {
  nav: {
    position: 'fixed', bottom: 0, left: '50%', transform: 'translateX(-50%)',
    width: '100%', maxWidth: 430, display: 'flex',
    background: '#fff', borderTop: '1px solid #eee',
    padding: '8px 0 12px', zIndex: 100
  },
  btn: {
    flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center',
    gap: 3, background: 'none', border: 'none', cursor: 'pointer'
  },
  label: { fontSize: 10, fontWeight: 500 }
}