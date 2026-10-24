import { Link, NavLink, Outlet } from 'react-router'
import useAuthenticated from './hooks/useAuthenticated'

export default function Layout() {
  const { authenticated } = useAuthenticated();

  return (
    <div className="flex min-h-screen flex-col">
      <nav className="flex items-center justify-between border-b border-line px-6 py-4">
        <NavLink to="/" className="text-lg font-bold tracking-tight text-accent">Interlude</NavLink>
        <div className="flex items-center gap-5">
          {!authenticated ? 
          <>
            <NavLink
              to="/Login"
              className={({ isActive }) =>
                `text-sm transition-colors ${isActive ? 'text-accent' : 'text-muted hover:text-text'}`
              }
            >
              Log in
            </NavLink>
            <Link to="/Signup" className="btn-primary !px-4 !py-1.5 text-sm">Sign up</Link>
          </>
          : null}
        </div>
      </nav>

      <main className="flex flex-1 flex-col"><Outlet /></main>

      <footer className="border-t border-line px-6 py-4 text-center text-xs text-muted">
        &copy; {new Date().getFullYear()} Interlude
      </footer>
    </div>
  )
}