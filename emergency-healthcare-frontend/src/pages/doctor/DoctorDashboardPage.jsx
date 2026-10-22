import { useEffect, useRef, useState } from 'react'
import { Activity, AlertTriangle, CheckCircle2, HeartPulse, RefreshCw, Stethoscope } from 'lucide-react'
import { apiError } from '../../services/api/client'
import { doctorService } from '../../services/doctor/doctorService'

export function DoctorDashboardPage() {
  const [cases, setCases] = useState([])
  const [selectedCode, setSelectedCode] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const refreshTimer = useRef(null)

  useEffect(() => {
    loadCases()
    refreshTimer.current = setInterval(() => loadCases(true), 10000)
    return () => {
      if (refreshTimer.current) clearInterval(refreshTimer.current)
    }
  }, [])

  async function loadCases(silent = false) {
    if (!silent) setError('')
    try {
      const response = await doctorService.cases()
      setCases(response)
      if (!selectedCode && response?.[0]?.trackingCode) {
        setSelectedCode(response[0].trackingCode)
      }
    } catch (requestError) {
      if (!silent) setError(apiError(requestError))
    } finally {
      if (!silent) setLoading(false)
    }
  }

  async function updateStatus(status, label) {
    if (!selectedCode) return
    setError('')
    setMessage('')
    try {
      await doctorService.updateStatus(selectedCode, status)
      setMessage(`${label} for ${selectedCode}`)
      loadCases()
    } catch (requestError) {
      setError(apiError(requestError))
    }
  }

  const selectedCase = cases.find((item) => item.trackingCode === selectedCode)

  return (
    <section className="dashboard-screen">
      <div className="dashboard-header">
        <div className="section-heading"><Stethoscope size={21} /><h1>Doctor Dashboard</h1></div>
        <div className="dashboard-actions">
          <button className="button ghost small" onClick={() => loadCases()} type="button" title="Refresh">
            <RefreshCw size={16} />
          </button>
        </div>
      </div>
      {message && <div className="alert success">{message}</div>}
      {error && <div className="alert critical">{error}</div>}
      <div className="dashboard-grid">
        <article className="workflow-panel">
          <h2>Assigned Cases ({cases.length})</h2>
          <div className="case-list">
            {loading && <p className="empty-state">Loading cases...</p>}
            {!loading && cases.length === 0 && (
              <p className="empty-state">No active cases assigned. Cases will appear here when hospitals assign you to emergency patients.</p>
            )}
            {cases.map((item) => (
              <button key={item.trackingCode} className={selectedCode === item.trackingCode ? 'case-row active' : 'case-row'} onClick={() => setSelectedCode(item.trackingCode)} type="button">
                <strong>{item.trackingCode}</strong>
                <span className="status-pill critical" style={{ fontSize: '0.7rem', minHeight: '22px' }}>{item.status}</span>
                <span>{item.patientName} — {item.emergencyType || 'Emergency'} — {item.severity}</span>
                <small>{item.hospital} {item.bed ? `• Bed ${item.bed}` : ''}</small>
              </button>
            ))}
          </div>
        </article>
        <article className="workflow-panel">
          <h2>Patient Details</h2>
          {selectedCase ? (
            <div className="resource-panel">
              <span><strong>Patient:</strong> {selectedCase.patientName}</span>
              <span><strong>Phone:</strong> {selectedCase.phone}</span>
              <span><strong>Type:</strong> {selectedCase.emergencyType}</span>
              <span><strong>Severity:</strong> {selectedCase.severity}</span>
              <span><strong>Status:</strong> {selectedCase.status}</span>
              <span><strong>Hospital:</strong> {selectedCase.hospital}</span>
              <span><strong>Ambulance:</strong> {selectedCase.assignedAmbulance || 'Pending'}</span>
              <span><strong>Bed:</strong> {selectedCase.bed || 'Pending allocation'}</span>
            </div>
          ) : (
            <p className="empty-state">Select a case to view patient details.</p>
          )}
        </article>
        <article className="workflow-panel">
          <h2>Treatment Actions</h2>
          <div className="dashboard-actions" style={{ flexDirection: 'column' }}>
            <button className="button secondary small" onClick={() => updateStatus('UNDER_TREATMENT', 'Treatment started')} type="button" disabled={!selectedCode}>
              <Activity size={16} />
              Mark Under Treatment
            </button>
            <button className="button secondary small" onClick={() => updateStatus('CRITICAL', 'Marked critical')} type="button" disabled={!selectedCode} style={{ borderColor: 'rgba(255,59,48,0.4)' }}>
              <AlertTriangle size={16} />
              Mark Critical
            </button>
            <button className="button secondary small" onClick={() => updateStatus('STABLE', 'Marked stable')} type="button" disabled={!selectedCode} style={{ borderColor: 'rgba(46,204,113,0.4)' }}>
              <HeartPulse size={16} />
              Mark Stable
            </button>
            <button className="button secondary small" onClick={() => updateStatus('DISCHARGED', 'Patient discharged')} type="button" disabled={!selectedCode} style={{ borderColor: 'rgba(46,204,113,0.6)' }}>
              <CheckCircle2 size={16} />
              Discharge Patient
            </button>
          </div>
        </article>
      </div>
    </section>
  )
}
