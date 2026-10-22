import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { KeyRound, LogIn } from 'lucide-react'
import { useAuth } from '../../contexts/AuthContext.jsx'
import { apiError } from '../../services/api/client'

export function LoginPage() {
  const [email, setEmail] = useState('heart.ops@example.com')
  const [password, setPassword] = useState('password')
  const [otp, setOtp] = useState('')
  const [needsOtp, setNeedsOtp] = useState(false)
  const [error, setError] = useState('')
  const { login, verifyDriverOtp } = useAuth()
  const navigate = useNavigate()

  async function submit(event) {
    event.preventDefault()
    setError('')
    try {
      const response = needsOtp ? await verifyDriverOtp(otp) : await login(email, password)
      if (response.otpRequired) {
        setNeedsOtp(true)
        return
      }
      const route = response.role === 'ADMIN' ? '/admin' : response.role === 'DOCTOR' ? '/doctor' : response.role === 'DRIVER' ? '/driver' : '/hospital'
      navigate(route)
    } catch (requestError) {
      setError(apiError(requestError))
    }
  }

  return (
    <section className="center-screen">
      <form className="workflow-panel ops-form login-panel" onSubmit={submit}>
        <div className="section-heading">
          <KeyRound size={20} />
          <h1>Internal Login</h1>
        </div>
        {!needsOtp && (
          <>
            <label>Email<input value={email} onChange={(event) => setEmail(event.target.value)} /></label>
            <label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
            <p style={{ fontSize: '0.8rem', color: 'var(--muted)', marginTop: '8px' }}>Demo accounts: heart.ops@example.com, neuro.ops@example.com, etc.</p>
          </>
        )}
        {needsOtp && <label>Driver OTP<input value={otp} onChange={(event) => setOtp(event.target.value)} /></label>}
        {error && <div className="alert critical">{error}</div>}
        <button className="button primary" type="submit">
          <LogIn size={17} />
          {needsOtp ? 'Verify OTP' : 'Authenticate'}
        </button>
      </form>
    </section>
  )
}
