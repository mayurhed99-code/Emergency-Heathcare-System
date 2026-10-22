import { useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Ambulance, Bell, Bed, Building2, RefreshCw, Search, Stethoscope, Truck } from 'lucide-react'
import { Timeline } from '../../components/notifications/Timeline.jsx'
import { apiError } from '../../services/api/client'
import { emergencyService } from '../../services/emergency/emergencyService'

const presentationTimeline = [
  { label: 'Emergency Created', detail: 'Emergency request submitted', createdAt: null },
  { label: 'Hospital Assigned', detail: 'Selected hospital linked to emergency case', createdAt: null },
  { label: 'Ambulance Assigned', detail: 'Ambulance and driver assigned to dispatch', createdAt: null },
  { label: 'Hospital Notified', detail: 'Hospital command dashboard, SMS, and email notified', createdAt: null },
  { label: 'Doctor Assigned', detail: 'Doctor assigned by hospital command', createdAt: null },
  { label: 'Bed Allocated', detail: 'Hospital bed allocated to case', createdAt: null },
  { label: 'Treatment Started', detail: 'Care team marked treatment in progress', createdAt: null },
]

function timelineForPresentation(items = []) {
  const labels = new Set(items.map((item) => item.label))
  const missing = presentationTimeline
    .filter((item) => !labels.has(item.label))
    .map((item) => ({ ...item, detail: `Pending - ${item.detail}` }))
  return [...items, ...missing]
}

export function EmergencyTrackingPage() {
  const [searchParams] = useSearchParams()
  const [trackingCode, setTrackingCode] = useState(searchParams.get('code') || localStorage.getItem('ehs_last_tracking_code') || '')
  const [caseData, setCaseData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const refreshTimer = useRef(null)

  useEffect(() => {
    const code = searchParams.get('code') || localStorage.getItem('ehs_last_tracking_code')
    if (code) {
      setTrackingCode(code)
      loadCase(code)
    }
    return () => {
      if (refreshTimer.current) clearInterval(refreshTimer.current)
    }
  }, [searchParams])

  useEffect(() => {
    if (caseData && caseData.trackingCode) {
      if (refreshTimer.current) clearInterval(refreshTimer.current)
      refreshTimer.current = setInterval(() => {
        loadCase(caseData.trackingCode, true)
      }, 15000)
    }
    return () => {
      if (refreshTimer.current) clearInterval(refreshTimer.current)
    }
  }, [caseData?.trackingCode])

  async function loadCase(code = trackingCode, silent = false) {
    if (!code) return
    if (!silent) {
      setError('')
      setLoading(true)
    }
    try {
      const response = await emergencyService.track(code)
      setCaseData(response)
      localStorage.setItem('ehs_last_emergency', JSON.stringify(response))
      localStorage.setItem('ehs_last_tracking_code', response.trackingCode)
    } catch (requestError) {
      if (!silent) {
        localStorage.removeItem('ehs_last_emergency')
        localStorage.removeItem('ehs_last_tracking_code')
        setCaseData(null)
        setError(apiError(requestError))
      }
    } finally {
      if (!silent) setLoading(false)
    }
  }

  return (
    <section className="tracking-screen">
      <div className="tracking-header">
        <div>
          <span className="status-pill">Tracking Console</span>
          <h1>Emergency Tracking</h1>
        </div>
        <div className="tracking-search">
          <input value={trackingCode} onChange={(event) => setTrackingCode(event.target.value)} placeholder="EMG-XXXXXXXX" />
          <button className="button secondary small" onClick={() => loadCase()} type="button" disabled={loading}>
            <Search size={16} />
            {loading ? 'Searching...' : 'Track'}
          </button>
          {caseData && (
            <button className="button ghost small" onClick={() => loadCase(caseData.trackingCode)} type="button" title="Refresh">
              <RefreshCw size={16} />
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="alert critical">
          <strong>{error}</strong>
          <p style={{ marginTop: '8px', color: 'var(--muted)' }}>
            This tracking code was not found. It may have expired or the database was reset.{' '}
            <Link to="/emergency" style={{ color: 'var(--primary)' }}>Create a new emergency request</Link>
          </p>
        </div>
      )}

      {!caseData && !error && !loading && (
        <div className="workflow-panel" style={{ textAlign: 'center', padding: '48px 22px' }}>
          <Search size={48} style={{ color: 'var(--muted)', marginBottom: '16px' }} />
          <h2>Enter a Tracking Code</h2>
          <p style={{ color: 'var(--muted)', marginTop: '8px' }}>
            Enter your emergency tracking code (e.g., EMG-XXXXXXXX) to view real-time status updates.
          </p>
          <p style={{ color: 'var(--muted)', marginTop: '12px' }}>
            Don&apos;t have a tracking code?{' '}
            <Link to="/emergency" style={{ color: 'var(--primary)' }}>Request emergency assistance</Link>
          </p>
        </div>
      )}

      {caseData && (
        <>
          <div className="alert success" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <RefreshCw size={14} />
            <span>Live tracking active — auto-refreshing every 15 seconds</span>
          </div>
          <div className="tracking-grid">
            <article className="workflow-panel patient-card">
              <h2>{caseData.patientName}</h2>
              <strong>{caseData.trackingCode}</strong>
              <span>{caseData.phone}</span>
              <span className="status-pill critical">{caseData.status}</span>
              <span style={{ color: 'var(--muted)', fontSize: '0.85rem' }}>{caseData.emergencyType} — {caseData.severity}</span>
            </article>
            <article className="workflow-panel assignment-card">
              <Building2 size={19} />
              <span>Hospital</span>
              <strong>{caseData.hospital || 'Pending assignment'}</strong>
              <small>{caseData.hospitalDistanceKm ? `${caseData.hospitalDistanceKm} km simulated distance` : ''}</small>
            </article>
            <article className="workflow-panel assignment-card">
              <Truck size={19} />
              <span>Assigned Ambulance</span>
              <strong>{caseData.assignedAmbulance || 'Pending'}</strong>
              <small>{caseData.driver || 'Driver pending'}{caseData.ambulanceDistanceKm ? ` / ${caseData.ambulanceDistanceKm} km away` : ''}</small>
            </article>
            <article className="workflow-panel assignment-card">
              <Stethoscope size={19} />
              <span>Doctor Assignment</span>
              <strong>{caseData.doctor || 'Pending hospital action'}</strong>
            </article>
            <article className="workflow-panel assignment-card">
              <Bed size={19} />
              <span>Bed Allocation</span>
              <strong>{caseData.bed || 'Pending hospital action'}</strong>
            </article>
            <article className="workflow-panel timeline-panel">
              <div className="section-heading">
                <Bell size={20} />
                <h2>Notification Timeline</h2>
              </div>
              <Timeline items={timelineForPresentation(caseData.notificationTimeline)} />
            </article>
          </div>
        </>
      )}
    </section>
  )
}
