import { useEffect, useState } from 'react'
import { Plus, Search, Check, AlertCircle, Stethoscope } from 'lucide-react'
import { hospitalService } from '../../services/hospital/hospitalService'
import { apiError } from '../../services/api/client'
import { Modal } from '../common/Modal.jsx'

export function DoctorManagement({ openModalTrigger }) {
  const [doctors, setDoctors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [showAddForm, setShowAddForm] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [newDoctor, setNewDoctor] = useState({ name: '', email: '', phone: '', specialization: 'EMERGENCY_MEDICINE' })

  useEffect(() => {
    loadDoctors()
  }, [])

  useEffect(() => {
    if (openModalTrigger) setShowAddForm(true)
  }, [openModalTrigger])

  async function loadDoctors() {
    try {
      const data = await hospitalService.getDoctors()
      setDoctors(data)
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
      await hospitalService.addDoctor(newDoctor)
      setShowAddForm(false)
      setNewDoctor({ name: '', email: '', phone: '', specialization: 'EMERGENCY_MEDICINE' })
      setSuccess('Doctor added successfully.')
      setTimeout(() => setSuccess(''), 3000)
      loadDoctors()
    } catch (err) {
      setError(apiError(err))
    } finally {
      setIsSubmitting(false)
    }
  }

  const filteredDoctors = doctors.filter(doc => 
    doc.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    doc.specialization.toLowerCase().includes(searchQuery.toLowerCase())
  ).sort((a, b) => (a.available === b.available) ? 0 : a.available ? -1 : 1)

  return (
    <section className="workflow-panel resource-panel" style={{ display: 'flex', flexDirection: 'column' }}>
      <div className="section-heading" style={{ justifyContent: 'space-between', marginBottom: '16px' }}>
        <h2>Doctors</h2>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          <div className="search-bar" style={{ position: 'relative' }}>
            <Search size={14} style={{ position: 'absolute', left: '8px', top: '50%', transform: 'translateY(-50%)', color: 'var(--muted)' }} />
            <input 
              type="text" 
              placeholder="Search doctors..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              style={{ paddingLeft: '28px', paddingRight: '8px', paddingBottom: '4px', paddingTop: '4px', fontSize: '0.8rem', width: '150px' }}
            />
          </div>
          <button className="button primary small" onClick={() => setShowAddForm(true)}><Plus size={14} /> Add</button>
        </div>
      </div>
      
      {success && <div className="alert success" style={{ padding: '8px', marginBottom: '12px' }}><Check size={14} /> {success}</div>}
      
      <Modal isOpen={showAddForm} onClose={() => setShowAddForm(false)} title="Add New Doctor">
        {error && <div className="alert critical"><AlertCircle size={14} /> {error}</div>}
        <form onSubmit={handleAdd} className="ops-form" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <label>Full Name
            <input placeholder="Dr. Jane Doe" required value={newDoctor.name} onChange={e => setNewDoctor({...newDoctor, name: e.target.value})} />
          </label>
          <label>Email Address
            <input placeholder="jane.doe@hospital.com" required type="email" value={newDoctor.email} onChange={e => setNewDoctor({...newDoctor, email: e.target.value})} />
          </label>
          <label>Phone Number
            <input placeholder="+1 555-0198" required value={newDoctor.phone} onChange={e => setNewDoctor({...newDoctor, phone: e.target.value})} />
          </label>
          <label>Specialization
            <select value={newDoctor.specialization} onChange={e => setNewDoctor({...newDoctor, specialization: e.target.value})}>
              <option value="EMERGENCY_MEDICINE">Emergency Medicine</option>
              <option value="CARDIOLOGY">Cardiology</option>
              <option value="NEUROLOGY">Neurology</option>
              <option value="BURN_UNIT">Burn Unit</option>
              <option value="TRAUMA_CENTER">Trauma Center</option>
              <option value="ICU">ICU</option>
            </select>
          </label>
          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '8px' }}>
            <button type="button" className="button ghost" onClick={() => setShowAddForm(false)}>Cancel</button>
            <button type="submit" className="button primary" disabled={isSubmitting}>
              {isSubmitting ? 'Saving...' : 'Save Doctor'}
            </button>
          </div>
        </form>
      </Modal>

      <div className="case-list" style={{ flex: 1, overflowY: 'auto' }}>
        {loading ? <p className="empty-state">Loading doctors...</p> : 
          filteredDoctors.length === 0 ? (
            <p className="empty-state">
              {searchQuery ? "No doctors found matching search." : "No doctors registered. Click 'Add' to onboard staff."}
            </p>
          ) : filteredDoctors.map(doc => (
          <div key={doc.id} className="case-row" style={{ cursor: 'default', display: 'flex', alignItems: 'center', gap: '12px', padding: '12px 16px' }}>
            <div style={{ background: 'var(--bg-hover)', padding: '10px', borderRadius: '50%', color: 'var(--primary)' }}>
              <Stethoscope size={20} />
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
              <strong style={{ fontSize: '1rem' }}>{doc.name}</strong>
              <div style={{ display: 'flex', gap: '8px', color: 'var(--muted)', fontSize: '0.8rem', marginTop: '2px' }}>
                <span>{doc.specialization.replace('_', ' ')}</span>
                <span>•</span>
                <span>{doc.phone}</span>
              </div>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
              <span className={`status-pill ${doc.available ? 'success' : 'warning'}`}>
                {doc.available ? 'Available' : 'Busy'}
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>On Duty</span>
            </div>
          </div>
        ))}
      </div>
    </section>
  )
}
