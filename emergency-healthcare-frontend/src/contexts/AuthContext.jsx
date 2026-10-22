import { createContext, useContext, useMemo, useState } from 'react'
import { authService } from '../services/auth/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [session, setSession] = useState(() => {
    const token = localStorage.getItem('ehs_token')
    const role = localStorage.getItem('ehs_role')
    const name = localStorage.getItem('ehs_name')
    return token ? { token, role, name } : null
  })
  const [pendingDriverEmail, setPendingDriverEmail] = useState('')

  async function login(email, password) {
    const response = await authService.login({ email, password })
    if (response.otpRequired) {
      setPendingDriverEmail(email)
      return response
    }
    saveSession(response)
    return response
  }

  async function verifyDriverOtp(otp) {
    const response = await authService.verifyDriverOtp({ email: pendingDriverEmail, otp })
    saveSession(response)
    setPendingDriverEmail('')
    return response
  }

  function saveSession(response) {
    localStorage.setItem('ehs_token', response.token)
    localStorage.setItem('ehs_role', response.role)
    localStorage.setItem('ehs_name', response.name)
    setSession({ token: response.token, role: response.role, name: response.name })
  }

  function logout() {
    localStorage.removeItem('ehs_token')
    localStorage.removeItem('ehs_role')
    localStorage.removeItem('ehs_name')
    setSession(null)
  }

  const value = useMemo(
    () => ({ session, pendingDriverEmail, login, verifyDriverOtp, logout }),
    [session, pendingDriverEmail],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  return useContext(AuthContext)
}
