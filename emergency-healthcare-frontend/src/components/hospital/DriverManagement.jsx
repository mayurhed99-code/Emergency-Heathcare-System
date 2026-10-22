import { useEffect, useState } from 'react'
import { Plus, Search, Check, AlertCircle, UserCircle } from 'lucide-react'
import { hospitalService } from '../../services/hospital/hospitalService'
import { apiError } from '../../services/api/client'
import { Modal } from '../common/Modal.jsx'

export function DriverManagement({ openModalTrigger }) {
  const [drivers, setDrivers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showAddForm, setShowAddForm] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [newDriver, setNewDriver] = useState({ name: '', email: '', phone: '', licenseNumber: '' })

  useEffect(() => {
    loadDrivers()
  }, [])

  useEffect(() => {
    if (openModalTrigger) setShowAddForm(true)
  }, [openModalTrigger])

  async function loadDrivers() {
    try {
      const data = await hospitalService.getDrivers()
      setDrivers(data)
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
      await hospitalService.addDriver(newDriver)
      setShowAddForm(false)
      setNewDriver({ name: '', email: '', phone: '', licenseNumber: '' })
      setSuccess('Driver added successfully.')
      setTimeout(() => setSuccess(''), 3000)
      loadDrivers()
    } catch (err) {
      setError(apiError(err))
    } finally {
      setIsSubmitting(false)
    }
  }

  const filteredDrivers = drivers.filter(drv => 
    drv.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    (drv.licenseNumber && drv.licenseNumber.toLowerCase().includes(searchQuery.toLowerCase()))
  ).sort((a, b) => (a.available === b.available) ? 0 : a.available ? -1 : 1)

  return (
    <section className="workflow-panel resource-panel" style={{ display: 'flex', flexDirection: 'column' }}>
      <div className="section-heading" style={{ justifyContent: 'space-between', marginBottom: '16px' }}>
        <h2>Drivers</h2>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <div className="search-bar" style={{ position: 'relative' }}>
            <Search size={14} style={{ position: 'absolute', left: '8px', top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)' }} />
            <input 
              type="text" 
              placeholder="Search drivers..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              style={{ paddingLeft: '28px', paddingRight: '8px', paddingBottom: '4px', paddingTop: '4px', fontSize: '0.8rem', width: '150px' }}
            />
          </div>
          <button className="button primary small" onClick={() => setShowAddForm(true)}><Plus size={14} /> Add</button>
        </div>
      </div>
      
      {success && <div className="alert success" style={{ padding: '8px', marginBottom: '12px' }}><Check size={14} /> {success}</div>}
      
      <Modal isOpen={showAddForm} onClose={() => setShowAddForm(false)} title="Add New Driver">
        {error && <div className="alert critical"><AlertCircle size={14} /> {error}</div>}
        <form onSubmit={handleAdd} className="ops-form" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <label>Full Name
            <input placeholder="John Smith" required value={newDriver.name} onChange={e => setNewDriver({...newDriver, name: e.target.value})} />
          </label>
          <label>Email Address
            <input placeholder="john.smith@hospital.com" required type="email" value={newDriver.email} onChange={e => setNewDriver({...newDriver, email: e.target.value})} />
          </label>
          <label>Phone Number
            <input placeholder="+1 555-0199" required value={newDriver.phone} onChange={e => setNewDriver({...newDriver, phone: e.target.value})} />
          </label>
          <label>License Number
            <input placeholder="DL-12345678" required value={newDriver.licenseNumber} onChange={e => setNewDriver({...newDriver, licenseNumber: e.target.value})} />
          </label>
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '8px' }}>
            <button type="button" className="button ghost" onClick={() => setShowAddForm(false)}>Cancel</button>
            <button type="submit" className="button primary" disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save Driver'}
            </button>
          </div>
        </form>
      </Modal>

      <div className="case-list" style={{ flex: 1, overflowY: 'auto' }}>
        {loading ? <p className="empty-state">Loading drivers...</p> : 
          filteredDrivers.length === 0 ? (
            <p className="empty-state">
              {searchQuery ? "No drivers found matching search." : "No drivers registered. Click 'Add' to onboard staff."}
            </p>
          ) : filteredDrivers.map(drv => (
          <div key={drv.id} className="case-row" style={{ cursor: 'default', display: 'flex', alignItems: 'center', gap: '12px', padding: '12px 16px' }}>
            <div style={{ background: 'var(--bg-hover)', padding: '10px', borderRadius: '50%', color: 'var(--primary)' }}>
              <UserCircle size={20} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
              <strong style={{ fontSize: '1rem' }}>{drv.name}</strong>
              <div style={{ display: 'flex', gap: '8px', color: 'var(--muted)', fontSize: '0.8rem', marginTop: '2px' }}>
                <span>ID: {drv.licenseNumber || 'N/A'}</span>
                <span>•</span>
                <span>Trips: <strong style={{ color: 'var(--text-primary)' }}>{drv.completedTrips || 0}</strong></span>
              </div>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
              <span className={`status-pill ${drv.available ? 'success' : 'warning'}`}>
                {drv.available ? 'Available' : 'Busy'}
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>On Duty</span>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
