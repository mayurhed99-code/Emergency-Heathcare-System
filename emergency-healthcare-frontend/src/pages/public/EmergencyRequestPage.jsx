import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Ambulance, LocateFixed, Send } from 'lucide-react'
import { apiError } from '../../services/api/client'
import { emergencyService } from '../../services/emergency/emergencyService'

const initialForm = {
  name: '',
  phone: '',
  email: '',
  emergencyType: 'HEART_ATTACK',
  severity: 'CRITICAL',
  locationText: 'MG Road, Bengaluru',
  latitude: 12.9716,
  longitude: 77.5946,
}

const demoCandidates = [
  { name: 'City Heart Emergency Institute', simulatedDistanceKm: 3.8, specializationMatch: 'CARDIOLOGY', availableBeds: 47, availableAmbulances: 8 },
  { name: 'Apex Multi-Specialty Emergency Center', simulatedDistanceKm: 6.4, specializationMatch: 'CARDIOLOGY', availableBeds: 52, availableAmbulances: 10 },
  { name: 'Northside ICU Network Hospital', simulatedDistanceKm: 8.1, specializationMatch: 'NEAREST_GENERAL_SUPPORT', availableBeds: 28, availableAmbulances: 5 },
  { name: 'Highway Trauma Response Hospital', simulatedDistanceKm: 10.2, specializationMatch: 'NEAREST_GENERAL_SUPPORT', availableBeds: 39, availableAmbulances: 9 },
  { name: 'NeuroCare Trauma Center', simulatedDistanceKm: 12.5, specializationMatch: 'NEAREST_GENERAL_SUPPORT', availableBeds: 31, availableAmbulances: 6 },
]

export function EmergencyRequestPage() {
  const [form, setForm] = useState(initialForm)
  const [candidates, setCandidates] = useState(demoCandidates)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    let ignore = false
    emergencyService
      .nearbyHospitals({ emergencyType: form.emergencyType, locationText: form.locationText })
      .then((data) => !ignore && setCandidates(data))
      .catch(() => !ignore && setCandidates(demoCandidates))
    return () => {
      ignore = true
    }
  }, [form.emergencyType, form.locationText])

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function submit(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    try {
      const response = await emergencyService.create(form)
      localStorage.setItem('ehs_last_emergency', JSON.stringify(response))
      localStorage.setItem('ehs_last_tracking_code', response.trackingCode)
      navigate(`/track?code=${response.trackingCode}`)
    } catch (requestError) {
      setError(apiError(requestError))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="workflow-layout">
      <div className="workflow-panel">
        <div className="section-heading">
          <Ambulance size={20} />
          <h1>Emergency Request</h1>
        </div>
        <form className="ops-form" onSubmit={submit}>
          <label>
            Patient Name
            <input name="name" value={form.name} onChange={updateField} required />
          </label>
          <label>
            Phone
            <input name="phone" value={form.phone} onChange={updateField} required />
          </label>
          <label>
            Email
            <input name="email" type="email" value={form.email} onChange={updateField} />
          </label>
          <div className="form-grid">
            <label>
              Emergency Type
              <select name="emergencyType" value={form.emergencyType} onChange={updateField}>
                <option value="HEART_ATTACK">Heart Attack</option>
                <option value="BRAIN_INJURY">Brain Injury</option>
                <option value="BURN_INJURY">Burn Injury</option>
                <option value="ROAD_ACCIDENT">Road Accident</option>
                <option value="BREATHING_PROBLEM">Breathing Problem</option>
              </select>
            </label>
            <label>
              Severity
              <select name="severity" value={form.severity} onChange={updateField}>
                <option value="CRITICAL">Critical</option>
                <option value="SERIOUS">Serious</option>
                <option value="MODERATE">Moderate</option>
              </select>
            </label>
          </div>
          <label>
            Location
            <span className="input-with-icon">
              <LocateFixed size={16} />
              <input name="locationText" value={form.locationText} onChange={updateField} required />
            </span>
          </label>
          {error && <div className="alert critical">{error}</div>}
          <button className="button primary" disabled={loading} type="submit">
            <Send size={17} />
            {loading ? 'Dispatching' : 'Create Emergency Workflow'}
          </button>
        </form>
      </div>
      <aside className="workflow-panel">
        <h2>Generated Nearby Hospitals</h2>
        <div className="candidate-list">
          {candidates.map((hospital, index) => (
            <article key={`${hospital.name}-${index}`}>
              <span className="rank">{index + 1}</span>
              <div>
                <strong>{hospital.name}</strong>
                <p>{hospital.specializationMatch} - {hospital.simulatedDistanceKm} km simulated</p>
              </div>
              <small>{hospital.availableBeds} beds / {hospital.availableAmbulances} ambulances</small>
            </article>
          ))}
        </div>
      </aside>
    </section>
  )
}
