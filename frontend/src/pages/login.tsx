import { useState, type SubmitEvent  } from 'react'
import { Link, useNavigate } from 'react-router'
import { ApiBridge } from '../bridge/api_bridge'

export default function Login() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: SubmitEvent) {
    e.preventDefault()
    setError(null)
    setBusy(true)
    try {
      const res = await ApiBridge.login({ email: email.trim().toLowerCase(), password })
      if (res instanceof Response) {
        setError(res.status === 401 ? 'Invalid email or password' : 'Something went wrong')
      } else {
        navigate('/')
      }
    } catch {
      setError("Can't reach the server")
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex flex-1 items-center justify-center px-6">
      <form onSubmit={submit} className="w-full max-w-sm space-y-4">
        <h1 className="text-2xl font-bold">Log in</h1>
        <input className="input" type="email" placeholder="Email" autoComplete="email" required
          value={email} onChange={(e) => setEmail(e.target.value)} />
        <input className="input" type="password" placeholder="Password" autoComplete="current-password" required
          value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <p role="alert" className="text-sm text-danger">{error}</p>}
        <button className="btn-primary w-full" disabled={busy}>{busy ? 'Logging in…' : 'Log in'}</button>
        <p className="text-center text-sm text-muted">
          No account? <Link to="/signup" className="text-accent hover:underline">Sign up</Link>
        </p>
      </form>
    </div>
  )
}