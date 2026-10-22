import { useEffect, useState } from 'react'
import { BarChart3, Building2, HeartPulse, RefreshCw, Shield, Users } from 'lucide-react'
import { MetricCard } from '../../components/common/MetricCard.jsx'
import { apiError } from '../../services/api/client'
import { adminService } from '../../services/admin/adminService'

export function AdminDashboardPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDashboard()
  }, [])

  async function loadDashboard() {
    setError('')
    try {
      const response = await adminService.dashboard()
      setData(response)
    } catch (requestError) {
      setError(apiError(requestError))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="dashboard-screen">
      <div className="dashboard-header">
        <div className="section-heading"><Shield size={21} /><h1>Admin Dashboard</h1></div>
        <div className="dashboard-actions">
          <button className="button ghost small" onClick={loadDashboard} type="button" title="Refresh">
            <RefreshCw size={16} />
          </button>
        </div>
      </div>
      {error && <div className="alert critical">{error}</div>}
      {loading && <p className="empty-state">Loading admin dashboard...</p>}
      {data && (
        <>
          <div className="ops-metrics dashboard-metrics">
            <MetricCard label="Total Hospitals" value={data.totalHospitals} />
            <MetricCard label="Total Emergencies" value={data.totalEmergencies} tone="critical" />
            <MetricCard label="Total Admissions" value={data.totalAdmissions} tone="warning" />
            <MetricCard label="Total Transfers" value={data.totalTransfers} tone="success" />
          </div>
          <div className="dashboard-grid">
            {data.hospitals?.map((hospital) => (
              <article className="workflow-panel" key={hospital.id}>
                <div className="section-heading" style={{ marginBottom: '12px' }}>
                  <Building2 size={18} />
                  <h2>{hospital.name}</h2>
                </div>
                <div className="resource-panel">
                  <span><strong>City:</strong> {hospital.city}</span>
                  <span><strong>Total Beds:</strong> {hospital.totalBeds}</span>
                  <span><strong>Available Beds:</strong> <span style={{ color: hospital.availableBeds > 10 ? 'var(--success)' : 'var(--critical)' }}>{hospital.availableBeds}</span></span>
                  <span><strong>Active Cases:</strong> <span style={{ color: hospital.activeCases > 0 ? 'var(--warning)' : 'var(--muted)' }}>{hospital.activeCases}</span></span>
                  <span><strong>Ambulances:</strong> {hospital.availableAmbulances}</span>
                </div>
              </article>
            ))}
          </div>
        </>
      )}
    </section>
  )
}
