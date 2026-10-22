import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from '../components/layout/AppShell.jsx'
import { ProtectedRoute } from '../components/common/ProtectedRoute.jsx'
import { LoginPage } from '../pages/auth/LoginPage.jsx'
import { AdminDashboardPage } from '../pages/admin/AdminDashboardPage.jsx'
import { DoctorDashboardPage } from '../pages/doctor/DoctorDashboardPage.jsx'
import { DriverDashboardPage } from '../pages/driver/DriverDashboardPage.jsx'
import { HospitalDashboardPage } from '../pages/hospital/HospitalDashboardPage.jsx'
import { AdmissionWizardPage } from '../pages/public/AdmissionWizardPage.jsx'
import { EmergencyRequestPage } from '../pages/public/EmergencyRequestPage.jsx'
import { EmergencyTrackingPage } from '../pages/public/EmergencyTrackingPage.jsx'
import { HomePage } from '../pages/public/HomePage.jsx'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route index element={<HomePage />} />
        <Route path="/emergency" element={<EmergencyRequestPage />} />
        <Route path="/track" element={<EmergencyTrackingPage />} />
        <Route path="/admission" element={<AdmissionWizardPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/hospital"
          element={
            <ProtectedRoute allowedRoles={['HOSPITAL', 'ADMIN']}>
              <HospitalDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/doctor"
          element={
            <ProtectedRoute allowedRoles={['DOCTOR', 'ADMIN']}>
              <DoctorDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/driver"
          element={
            <ProtectedRoute allowedRoles={['DRIVER', 'ADMIN']}>
              <DriverDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}
