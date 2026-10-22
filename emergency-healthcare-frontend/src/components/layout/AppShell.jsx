import { Activity, Ambulance, Bell, Building2, LogOut, ShieldCheck, Stethoscope, Truck } from 'lucide-react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext.jsx'

const publicItems = [
  { to: '/emergency', label: 'Emergency', icon: Ambulance },
  { to: '/admission', label: 'Admission', icon: Activity },
  { to: '/track', label: 'Tracking', icon: Bell },
]

const roleItems = {
  HOSPITAL_STAFF: [{ to: '/hospital', label: 'Hospital', icon: Building2 }],
  DOCTOR: [{ to: '/doctor', label: 'Doctor', icon: Stethoscope }],
  DRIVER: [{ to: '/driver', label: 'Driver', icon: Truck }],
  ADMIN: [
    { to: '/hospital', label: 'Hospital', icon: Building2 },
    { to: '/doctor', label: 'Doctor', icon: Stethoscope },
    { to: '/driver', label: 'Driver', icon: Truck },
    { to: '/admin', label: 'Admin', icon: ShieldCheck },
  ],
}

const roleBadge = {
  HOSPITAL_STAFF: 'Hospital Staff',
  DOCTOR: 'Doctor',
  DRIVER: 'Driver',
  ADMIN: 'Administrator',
}

export function AppShell() {
  const { session, logout } = useAuth()

  const staffItems = session ? roleItems[session.role] || [] : []
  const navItems = [...publicItems, ...staffItems]

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" className="brand">
          <span className="brand-mark">EH</span>
          <span>Emergency Ops</span>
        </Link>
        <nav className="nav-strip" aria-label="Primary navigation">
          {navItems.map((item) => {
            const Icon = item.icon
            return (
              <NavLink key={item.to} to={item.to} className="nav-link">
                <Icon size={16} />
                <span>{item.label}</span>
              </NavLink>
            )
          })}
        </nav>
        <div className="session-block">
          {session ? (
            <>
              <span className="session-role-badge">{roleBadge[session.role] || session.role}</span>
              <span>{session.name}</span>
              <button type="button" className="icon-button" onClick={logout} aria-label="Logout">
                <LogOut size={16} />
              </button>
            </>
          ) : (
            <Link className="button ghost small" to="/login">
              Staff Login
            </Link>
          )}
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
