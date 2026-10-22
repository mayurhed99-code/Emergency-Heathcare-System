import { useEffect, useRef, useState } from 'react'
import { Ambulance, MapPin, RefreshCw, Truck } from 'lucide-react'
import { apiError } from '../../services/api/client'
import { driverService } from '../../services/driver/driverService'

export function DriverDashboardPage() {
  const [assignments, setAssignments] = useState([])
  const [selectedCode, setSelectedCode] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const refreshTimer = useRef(null)

  useEffect(() => {
    loadAssignments()
    refreshTimer.current = setInterval(() => loadAssignments(true), 10000)
    return () => {
      if (refreshTimer.current) clearInterval(refreshTimer.current)
    }
  }, [])

  async function loadAssignments(silent = false) {
    if (!silent) setError('')
    try {
      const response = await driverService.assignments()
      setAssignments(response)
      if (!selectedCode && response?.[0]?.trackingCode) {
        setSelectedCode(response[0].trackingCode)
      }
    } catch (requestError) {
      if (!silent) setError(apiError(requestError))
    } finally {
      if (!silent) setLoading(false)
    }
  }

  const selectedCase = assignments.find((item) => item.trackingCode === selectedCode)

  return (
    <section className="dashboard-screen">
      <div className="dashboard-header">
        <div className="section-heading"><Truck size={21} /><h1>Driver Dashboard</h1></div>
        <div className="dashboard-actions">
          <button className="button ghost small" onClick={() => loadAssignments()} type="button" title="Refresh">
            <RefreshCw size={16} />
          </button>
        </div>
      </div>
      {error && <div className="alert critical">{error}</div>}
      <div className="dashboard-grid">
        <article className="workflow-panel">
          <h2>Active Assignments ({assignments.length})</h2>
          <div className="case-list">
            {loading && <p className="empty-state">Loading assignments...</p>}
            {!loading && assignments.length === 0 && (
              <p className="empty-state">No active dispatch assignments. Assignments will appear here when emergencies are created and ambulances are dispatched.</p>
            )}
            {assignments.map((item) => (
              <button key={item.trackingCode} className={selectedCode === item.trackingCode ? 'case-row active' : 'case-row'} onClick={() => setSelectedCode(item.trackingCode)} type="button">
                <strong>{item.trackingCode}</strong>
                <span className="status-pill critical" style={{ fontSize: '0.7rem', minHeight: '22px' }}>{item.status}</span>
                <span>{item.patientName} — {item.severity}</span>
                <small>Pickup to {item.hospital}</small>
              </button>
            ))}
          </div>
        </article>
        <article className="workflow-panel">
          <h2>Dispatch Details</h2>
          {selectedCase ? (
            <div className="resource-panel">
              <span><strong>Patient:</strong> {selectedCase.patientName}</span>
              <span><strong>Phone:</strong> {selectedCase.phone}</span>
              <span><strong>Emergency:</strong> {selectedCase.emergencyType} — {selectedCase.severity}</span>
              <span><strong>Status:</strong> {selectedCase.status}</span>
              <span><strong>Destination:</strong> {selectedCase.hospital}</span>
              <span><strong>Ambulance:</strong> {selectedCase.assignedAmbulance || 'N/A'}</span>
              <span><strong>Hospital Distance:</strong> {selectedCase.hospitalDistanceKm ? `${selectedCase.hospitalDistanceKm} km` : 'N/A'}</span>
              <span><strong>Ambulance Distance:</strong> {selectedCase.ambulanceDistanceKm ? `${selectedCase.ambulanceDistanceKm} km` : 'N/A'}</span>
            </div>
          ) : (
            <p className="empty-state">Select an assignment to view dispatch details.</p>
          )}
        </article>
        <article className="workflow-panel">
          <h2>Navigation</h2>
          {selectedCase ? (
            <div className="resource-panel">
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--primary)' }}>
                <MapPin size={18} />
                <strong>Pickup Location → {selectedCase.hospital}</strong>
              </div>
              <span style={{ color: 'var(--muted)' }}>
                ETA: {selectedCase.ambulanceDistanceKm ? `~${Math.round(selectedCase.ambulanceDistanceKm * 3)} min` : 'Calculating...'}
              </span>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '8px' }}>
                <Ambulance size={18} />
                <span>{selectedCase.assignedAmbulance || 'Ambulance'} dispatched</span>
              </div>
            </div>
          ) : (
            <p className="empty-state">Select an assignment to view navigation details.</p>
          )}
        </article>
      </div>
    </section>
  )
}
