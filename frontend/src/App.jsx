import { Routes, Route, Link } from 'react-router-dom'
import { LoginPage, RegisterPage, ProfilePage } from './features/auth'
import { AchievementAdminPage, AchievementsPage } from './features/achievements'
import { JoinClanPage } from './features/clan/pages/JoinClanPage'
import { MyClanPage } from './features/clan/pages/MyClanPage'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'

function Home() {
  return (
    <>
      <section id="center">
        <div className="home-top-actions">
          <Link to="/clans/join" className="btn-primary" style={{ textDecoration: 'none' }}>
            Join a Clan
          </Link>
        </div>
        <div className="hero">
          <img src={heroImg} className="base" width="170" height="179" alt="" />
          <img src={reactLogo} className="framework" alt="React logo" />
          <img src={viteLogo} className="vite" alt="Vite logo" />
        </div>
        <div>
          <h1>Selamat Datang di Yomu</h1>
          <p>Tingkatkan literasimu, mulai hari ini.</p>
        </div>
        <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
          <Link to="/login" className="btn-primary" style={{ textDecoration: 'none' }}>Login</Link>
          <Link to="/register" className="btn-primary" style={{ backgroundColor: '#1e293b', textDecoration: 'none' }}>Register</Link>
          <Link to="/achievements" className="btn-primary" style={{ backgroundColor: '#0f766e', textDecoration: 'none' }}>Achievement</Link>
        </div>
      </section>

      <div className="ticks"></div>
    </>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/profile" element={<ProfilePage />} />
      <Route path="/achievements" element={<AchievementsPage />} />
      <Route path="/achievements/admin" element={<AchievementAdminPage />} />
      <Route path="/clans/join" element={<JoinClanPage />} />
      <Route path="/clans/my" element={<MyClanPage />} />
    </Routes>
  )
}

export default App
