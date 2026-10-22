import { useEffect, useState } from 'react'
import { Plus, Search, Check, AlertCircle, Truck } from 'lucide-react'
import { hospitalService } from '../../services/hospital/hospitalService'
import { apiError } from '../../services/api/client'
import { Modal } from '../common/Modal.jsx'

export function AmbulanceManagement({ openModalTrigger }) {
  const [ambulances, setAmbulances] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showAddForm, setShowAddForm] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [newAmbulance, setNewAmbulance] = useState({ callSign: '' })

  useEffect(() => {
    loadAmbulances()
  }, [])

  useEffect(() => {
    if (openModalTrigger) setShowAddForm(true)
  }, [openModalTrigger])

  async function loadAmbulances() {
    try {
      const data = await hospitalService.getAmbulances()
      setAmbulances(data)
    } catch (err) {
      setError(apiError(err))
    } finally {
      setLoading(false)
    }
  }

  async function handleAdd(e) {
    e.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      await hospitalService.addAmbulance(newAmbulance)
      setShowAddForm(false)
      setNewAmbulance({ callSign: '' })
      setSuccess('Ambulance added successfully.')
      setTimeout(() => setSuccess(''), 3000)
      loadAmbulances()
    } catch (err) {
      setError(apiError(err))
    } finally {
      setIsSubmitting(false)
    }
  }

  const filteredAmbulances = ambulances.filter(amb => 
    amb.callSign.toLowerCase().includes(searchQuery.toLowerCase()) || 
    (amb.driver && amb.driver.name.toLowerCase().includes(searchQuery.toLowerCase()))
  ).sort((a, b) => (a.status === b.status) ? 0 : a.status === 'AVAILABLE' ? -1 : 1)

  return (
    <section className="workflow-panel resource-panel" style={{ display: 'flex', flexDirection: 'column' }}>
      <div className="section-heading" style={{ justifyContent: 'space-between', marginBottom: '16px' }}>
        <h2>Ambulances</h2>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <div className="search-bar" style={{ position: 'relative' }}>
            <Search size={14} style={{ position: 'absolute', left: '8px', top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)' }} />
            <input 
              type="text" 
              placeholder="Search ambulances..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              style={{ paddingLeft: '28px', paddingRight: '8px', paddingBottom: '4px', paddingTop: '4px', fontSize: '0.8rem', width: '150px' }}
            />
          </div>
          <button className="button primary small" onClick={() => setShowAddForm(true)}><Plus size={14} /> Add</button>
        </div>
      </div>
      
      {success && <div className="alert success" style={{ padding: '8px', marginBottom: '12px' }}><Check size={14} /> {success}</div>}
      
      <Modal isOpen={showAddForm} onClose={() => setShowAddForm(false)} title="Add New Ambulance">
        {error && <div className="alert critical"><AlertCircle size={14} /> {error}</div>}
        <form onSubmit={handleAdd} className="ops-form" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <label>Call Sign
            <input placeholder="e.g. AMB-01" required value={newAmbulance.callSign} onChange={e => setNewAmbulance({...newAmbulance, callSign: e.target.value})} />
          </label>
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '8px' }}>
            <button type="button" className="button ghost" onClick={() => setShowAddForm(false)}>Cancel</button>
            <button type="submit" className="button primary" disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save Ambulance'}
            </button>
          </div>
        </form>
      </Modal>

      <div className="case-list" style={{ flex: 1, overflowY: 'auto' }}>
        {loading ? <p className="empty-state">Loading ambulances...</p> : 
          filteredAmbulances.length === 0 ? (
            <p className="empty-state">
              {searchQuery ? "No ambulances found matching search." : "No ambulances registered. Click 'Add' to register one."}
            </p>
          ) : filteredAmbulances.map(amb => (
          <div key={amb.id} className="case-row" style={{ cursor: 'default', display: 'flex', alignItems: 'center', gap: '12px', padding: '12px 16px' }}>
            <div style={{ background: 'var(--bg-hover)', padding: '10px', borderRadius: '50%', color: 'var(--primary)' }}>
              <Truck size={20} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
              <strong style={{ fontSize: '1rem' }}>{amb.callSign}</strong>
              <div style={{ display: 'flex', gap: '8px', color: 'var(--muted)', fontSize: '0.8rem', marginTop: '2px' }}>
                <span>Driver: <strong style={{ color: 'var(--text-primary)' }}>{amb.driver ? amb.driver.name : 'Unassigned'}</strong></span>
              </div>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
              <span className={`status-pill ${amb.status === 'AVAILABLE' ? 'success' : amb.status === 'MAINTENANCE' ? 'critical' : 'warning'}`}>
                {amb.status}
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>Fleet</span>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
