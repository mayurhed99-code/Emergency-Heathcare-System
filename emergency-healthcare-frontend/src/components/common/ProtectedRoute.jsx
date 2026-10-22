import { Navigate } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext.jsx'

export function ProtectedRoute({ children, allowedRoles }) {
  const { session } = useAuth()

  if (!session) {
    return <Navigate to="/login" replace />
  }

  if (allowedRoles && !allowedRoles.includes(session.role)) {
    return <Navigate to="/" replace />
  }

  return children
}
