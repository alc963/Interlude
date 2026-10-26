import { useState, type SubmitEvent  } from 'react'
import { Link, useNavigate } from 'react-router'
import { ApiBridge } from '../bridge/api_bridge'
import { validate_email, validate_password, validate_username } from '../bridge/validator'

export default function Signup() {
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function submit(e: SubmitEvent) {
    e.preventDefault()
    const problem = validate_username(username) ?? validate_email(email) ?? validate_password(password)
    if (problem) return setError(problem)

    setError(null)
    setBusy(true)
    try {
      const res = await ApiBridge.signup({ username, email: email.trim().toLowerCase(), password })
      if (res instanceof Response) {
        setError(res.status === 409 ? await res.text() : 'Something went wrong')
      } else {
        navigate('/connect')
      }
    } catch {
      setError("Can't reach the server")
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="flex flex-1 items-center justify-center px-6">
      <form onSubmit={submit} noValidate className="w-full max-w-sm space-y-4">
        <h1 className="text-2xl font-bold">Sign up</h1>
        <input className="input" placeholder="Username" autoComplete="username"
          value={username} onChange={(e) => setUsername(e.target.value)} />
        <input className="input" type="email" placeholder="Email" autoComplete="email"
          value={email} onChange={(e) => setEmail(e.target.value)} />
        <input className="input" type="password" placeholder="Password (8+ characters)" autoComplete="new-password"
          value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <p role="alert" className="text-sm text-danger">{error}</p>}
        <button className="btn-primary w-full" disabled={busy}>{busy ? 'Creating account…' : 'Create account'}</button>
        <p className="text-center text-sm text-muted">
          Have an account? <Link to="/login" className="text-accent hover:underline">Log in</Link>
        </p>
      </form>
    </div>
  )
}