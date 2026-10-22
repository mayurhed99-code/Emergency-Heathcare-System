import { useEffect, useRef, useState } from 'react'
import { Activity, Bell, Bed, Check, Hospital, RefreshCw, Send, Stethoscope, SwitchCamera, AlertTriangle, Info, Plus } from 'lucide-react'
import { Timeline } from '../../components/notifications/Timeline.jsx'
import { useAuth } from '../../contexts/AuthContext.jsx'
import { apiError } from '../../services/api/client'
import { hospitalService } from '../../services/hospital/hospitalService'
import { ResourceHealthPanel } from '../../components/hospital/ResourceHealthPanel.jsx'
import { DoctorManagement } from '../../components/hospital/DoctorManagement.jsx'
import { DriverManagement } from '../../components/hospital/DriverManagement.jsx'
import { AmbulanceManagement } from '../../components/hospital/AmbulanceManagement.jsx'
import { ConfirmDialog } from '../../components/common/ConfirmDialog.jsx'

const emptyDashboard = {
  emergencyQueue: [],
  plannedAdmissionsQueue: [],
  notifications: [],
  activeEmergencies: 0,
  plannedAdmissions: 0,
  transfers: 0,
  completedCases: 0,
}

export function HospitalDashboardPage() {
  const [dashboard, setDashboard] = useState(emptyDashboard)
  const [selectedCode, setSelectedCode] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const { session } = useAuth()
  const refreshTimer = useRef(null)

  // Triggers for modals
  const [addDoctorTrigger, setAddDoctorTrigger] = useState(0)
  const [addDriverTrigger, setAddDriverTrigger] = useState(0)
  const [addAmbulanceTrigger, setAddAmbulanceTrigger] = useState(0)

  // Confirm dialog state
  const [confirmState, setConfirmState] = useState({ isOpen: false, action: null, title: '', message: '' })

  useEffect(() => {
    refresh()
    refreshTimer.current = setInterval(refresh, 10000)
    return () => {
      if (refreshTimer.current) clearInterval(refreshTimer.current)
    }
  }, [session])

  async function refresh() {
    try {
      const response = await hospitalService.dashboard()
      setDashboard(response)
      if (!selectedCode && response.emergencyQueue?.filter(e => e.status !== 'DISCHARGED').length > 0) {
        setSelectedCode(response.emergencyQueue.filter(e => e.status !== 'DISCHARGED')[0].trackingCode)
      }
    } catch {
      // Keep current data on refresh failure
    } finally {
      setLoading(false)
    }
  }

  function handleActionClick(action) {
    setError('')
    setMessage('')
    if (!selectedCode) {
      setError('Select an emergency case from the queue first.')
      return
    }

    if (action === 'discharge') {
      setConfirmState({
        isOpen: true, action,
        title: 'Confirm Discharge',
        message: 'Are you sure you want to discharge this patient? This will release all assigned resources.'
      })
      return
    }
    if (action === 'transfer') {
      setConfirmState({
        isOpen: true, action,
        title: 'Confirm Transfer',
        message: 'Are you sure you want to initiate a transfer to another facility?'
      })
      return
    }
    
    executeAction(action)
  }

  async function executeAction(action) {
    try {
      const code = selectedCode
      if (action === 'accept') await hospitalService.acceptEmergency(code)
      if (action === 'doctor') await hospitalService.assignDoctor(code)
      if (action === 'bed') await hospitalService.allocateBed(code)
      if (action === 'transfer') await hospitalService.transferPatient(code, { reason: 'NO_ICU', details: 'ICU unavailable' })
      if (action === 'treatment') await hospitalService.updateStatus(code, 'UNDER_TREATMENT')
      if (action === 'discharge') await hospitalService.updateStatus(code, 'DISCHARGED')
      
      setMessage(`${action} action completed for ${code}`)
      setConfirmState({ isOpen: false, action: null, title: '', message: '' })
      refresh()
    } catch (requestError) {
      setError(apiError(requestError))
      setConfirmState({ isOpen: false, action: null, title: '', message: '' })
    }
  }

  const selectedCase = dashboard.emergencyQueue?.find((item) => item.trackingCode === selectedCode)
  const activeCases = dashboard.emergencyQueue?.filter(e => e.status !== 'DISCHARGED') || []
  const completedCasesList = dashboard.emergencyQueue?.filter(e => e.status === 'DISCHARGED') || []

  function getSeverityColor(severity) {
    if (severity === 'CRITICAL') return 'var(--critical)'
    if (severity === 'HIGH') return 'var(--warning)'
    return 'var(--success)'
  }

  return (
    <section className="dashboard-screen">
      <div className="dashboard-header" style={{ flexDirection: 'column', alignItems: 'stretch' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', background: 'var(--bg-elevated)', padding: '20px', borderRadius: '12px', border: '1px solid var(--border)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ background: 'rgba(59, 130, 246, 0.1)', color: 'var(--primary)', padding: '12px', borderRadius: '12px' }}>
              <Hospital size={32} />
            </div>
            <div>
              <h1 style={{ fontSize: '1.6rem', fontWeight: 'bold', margin: '0 0 4px 0', color: 'var(--text-primary)' }}>
                {session?.user?.email ? session.user.email.split('@')[0].toUpperCase().replace('.', ' ') + ' MEDICAL CENTER' : 'HOSPITAL COMMAND CENTER'}
              </h1>
              <div style={{ display: 'flex', gap: '12px', color: 'var(--muted)', fontSize: '0.9rem', alignItems: 'center' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}><div style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--success)' }}></div> System Online</span>
                <span>•</span>
                <span>ID: {session?.user?.id || 'SYS-01'}</span>
                <span>•</span>
                <span>Network: Connected</span>
              </div>
            </div>
          </div>
          <button className="button secondary" onClick={refresh} type="button" title="Refresh dashboard" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <RefreshCw size={16} /> Sync Status
          </button>
        </div>

        {/* QUICK ACTIONS */}
        <div style={{ display: 'flex', gap: '12px', marginBottom: '24px', flexWrap: 'wrap' }}>
          <button className="button secondary" onClick={() => setAddDoctorTrigger(prev => prev + 1)}><Plus size={16} /> Add Doctor</button>
          <button className="button secondary" onClick={() => setAddDriverTrigger(prev => prev + 1)}><Plus size={16} /> Add Driver</button>
          <button className="button secondary" onClick={() => setAddAmbulanceTrigger(prev => prev + 1)}><Plus size={16} /> Add Ambulance</button>
        </div>

        {/* CASE SUMMARY CARD */}
        {selectedCase && (
          <div className="workflow-panel" style={{ 
            marginBottom: '24px', 
            background: 'var(--bg-elevated)', 
            border: '1px solid var(--border)',
            borderTop: `4px solid ${getSeverityColor(selectedCase.severity)}`,
            padding: '24px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
              <div>
                <h3 style={{ margin: '0 0 8px 0', fontSize: '1.4rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {selectedCase.trackingCode} 
                  <span className="status-pill" style={{ fontSize: '0.8rem', backgroundColor: getSeverityColor(selectedCase.severity), color: '#fff' }}>
                    {selectedCase.severity}
                  </span>
                </h3>
                <div style={{ color: 'var(--muted)', fontSize: '0.9rem' }}>{selectedCase.patientName} • {selectedCase.emergencyType}</div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ fontSize: '0.8rem', color: 'var(--muted)', textTransform: 'uppercase' }}>Current Status</div>
                <div className="status-pill" style={{ fontSize: '1rem', padding: '6px 12px', marginTop: '4px' }}>{selectedCase.status}</div>
              </div>
            </div>
            
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', background: 'var(--bg-hover)', padding: '16px', borderRadius: '8px' }}>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--muted)', marginBottom: '4px' }}><Hospital size={14} style={{display:'inline', verticalAlign:'middle'}}/> Hospital</div>
                <div style={{ fontWeight: '500' }}>{selectedCase.hospital || 'Unassigned'}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--muted)', marginBottom: '4px' }}><Activity size={14} style={{display:'inline', verticalAlign:'middle'}}/> Ambulance</div>
                <div style={{ fontWeight: '500' }}>{selectedCase.assignedAmbulance || 'Pending'}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--muted)', marginBottom: '4px' }}><Stethoscope size={14} style={{display:'inline', verticalAlign:'middle'}}/> Doctor</div>
                <div style={{ fontWeight: '500' }}>{selectedCase.doctor || 'Pending'}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--muted)', marginBottom: '4px' }}><Bed size={14} style={{display:'inline', verticalAlign:'middle'}}/> Bed</div>
                <div style={{ fontWeight: '500' }}>{selectedCase.bed || 'Pending'}</div>
              </div>
            </div>
            
            <div className="dashboard-actions" style={{ marginTop: '24px', display: 'flex', gap: '12px', borderTop: '1px solid var(--border)', paddingTop: '16px' }}>
              {(selectedCase.status === 'AMBULANCE_ASSIGNED' || selectedCase.status === 'DOCTOR_REQUESTED') && (
                <button className="button primary" onClick={() => handleActionClick('accept')}><Check size={18} /> Accept Case</button>
              )}
              {selectedCase.status === 'ACCEPTED' && (
                <button className="button primary" onClick={() => handleActionClick('doctor')}><Stethoscope size={18} /> Assign Doctor</button>
              )}
              {selectedCase.status === 'DOCTOR_ASSIGNED' && (
                <button className="button primary" onClick={() => handleActionClick('bed')}><Bed size={18} /> Allocate Bed</button>
              )}
              {selectedCase.status === 'BED_ALLOCATED' && (
                <button className="button primary" onClick={() => handleActionClick('treatment')}><Activity size={18} /> Start Treatment</button>
              )}
              {selectedCase.status === 'UNDER_TREATMENT' && (
                <>
                  <button className="button primary" style={{ background: 'var(--success)' }} onClick={() => handleActionClick('discharge')}><Check size={18} /> Discharge</button>
                  <button className="button secondary" onClick={() => handleActionClick('transfer')}><SwitchCamera size={18} /> Transfer</button>
                </>
              )}
              {selectedCase.status === 'DISCHARGED' && (
                <div style={{ color: 'var(--success)', display: 'flex', alignItems: 'center', gap: '8px', fontWeight: '500' }}>
                  <Check size={18} /> Case successfully completed and closed.
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {message && <div className="alert success"><Check size={16} /> {message}</div>}
      {error && <div className="alert critical"><AlertTriangle size={16} /> {error}</div>}
      
      <ResourceHealthPanel dashboard={dashboard} />
      
      <div className="dashboard-grid">
        <section className="workflow-panel">
          <div className="section-heading"><Activity size={18} /> <h2>Active Emergencies</h2></div>
          <div className="case-list">
            {loading && <p className="empty-state">Loading cases...</p>}
            {!loading && activeCases.length === 0 && (
              <div className="empty-state" style={{ color: 'var(--success)', border: '1px solid var(--success)', background: 'rgba(34, 197, 94, 0.1)' }}>
                <Check size={24} style={{ marginBottom: '8px' }} />
                <p>✓ All emergency cases processed successfully.</p>
              </div>
            )}
            {activeCases.map((item) => (
              <button key={item.trackingCode} className={selectedCode === item.trackingCode ? 'case-row active' : 'case-row'} onClick={() => setSelectedCode(item.trackingCode)} type="button">
                <strong>{item.trackingCode}</strong>
                <span className="status-pill critical" style={{ fontSize: '0.7rem', minHeight: '22px' }}>{item.status}</span>
                <span>{item.patientName}</span>
                <small>{item.emergencyType}</small>
              </button>
            ))}
          </div>
        </section>

        <section className="workflow-panel">
          <div className="section-heading"><Check size={18} /> <h2>Completed Cases</h2></div>
          <div className="case-list">
            {completedCasesList.length === 0 && (
              <div className="empty-state">
                <Info size={24} style={{ marginBottom: '8px', color: 'var(--muted)' }} />
                <p>No completed cases today.</p>
              </div>
            )}
            {completedCasesList.map((item) => (
              <div key={item.trackingCode} className="case-row" style={{ display: 'grid', gridTemplateColumns: 'auto 1fr auto', gap: '16px', alignItems: 'center', padding: '12px', opacity: 0.85, cursor: 'default' }}>
                <div style={{ background: 'rgba(34, 197, 94, 0.1)', color: 'var(--success)', padding: '8px', borderRadius: '8px' }}>
                  <Check size={20} />
                </div>
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                  <strong style={{ fontSize: '1rem' }}>{item.trackingCode}</strong>
                  <span style={{ color: 'var(--text-secondary)' }}>{item.patientName}</span>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <span className="status-pill success" style={{ fontSize: '0.75rem' }}>{item.status}</span>
                  <div style={{ fontSize: '0.75rem', color: 'var(--muted)', marginTop: '4px' }}>Successfully Closed</div>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="workflow-panel timeline-panel" style={{ maxHeight: '400px', overflowY: 'auto' }}>
          <div className="section-heading" style={{ position: 'sticky', top: 0, background: 'var(--bg-elevated)', zIndex: 10, paddingBottom: '8px' }}>
            <Bell size={18} /><h2>Activity Feed</h2>
          </div>
          {dashboard.notifications?.length === 0 && <p className="empty-state">No activity logged.</p>}
          <Timeline items={dashboard.notifications} />
        </section>
        
        <DoctorManagement openModalTrigger={addDoctorTrigger} />
        <DriverManagement openModalTrigger={addDriverTrigger} />
        <AmbulanceManagement openModalTrigger={addAmbulanceTrigger} />
      </div>

      <ConfirmDialog 
        isOpen={confirmState.isOpen}
        title={confirmState.title}
        message={confirmState.message}
        onConfirm={() => executeAction(confirmState.action)}
        onCancel={() => setConfirmState({ isOpen: false, action: null, title: '', message: '' })}
      />
    </section>
  )
}
