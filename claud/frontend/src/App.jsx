import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Home from './pages/Home'
import Search from './pages/Search'
import Chat from './pages/Chat'
import Settings from './pages/Settings'
import BottomNav from './components/BottomNav'
 
export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/search" element={<Search />} />
        <Route path="/chat" element={<Chat />} />
        <Route path="/settings" element={<Settings />} />
      </Routes>
      <BottomNav />
    </BrowserRouter>
  )
}
 