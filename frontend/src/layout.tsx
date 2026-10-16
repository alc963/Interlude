// src/Layout.tsx
import { NavLink, Outlet } from 'react-router'
import './App.css'

export default function Layout() {
  return (
    <div className="app-container">
      <nav className="navbar">
        <NavLink to="/" className="logo">Interlude</NavLink>
        <div className="nav-links">
          <NavLink to="/history">History</NavLink>
          <NavLink to="/discover">Discover</NavLink>
          <NavLink to="/journal">Journal</NavLink>
        </div>
      </nav>

      <main>
        <Outlet />
      </main>

      <footer className="footer">
        <p>&copy; {new Date().getFullYear()} Interlude. All rights reserved.</p>
      </footer>
    </div>
  )
}