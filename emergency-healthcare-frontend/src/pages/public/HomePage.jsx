import { ArrowRight, Bed, HeartPulse, Hospital, Route, ShieldAlert } from 'lucide-react'
import { Link } from 'react-router-dom'
import { OperationsPreview } from '../../components/emergency/OperationsPreview.jsx'

const workflow = ['Citizen', 'Ambulance', 'Hospital', 'Doctor']
const emergencyTypes = ['Heart Attack', 'Brain Injury', 'Burn Injury', 'Road Accident', 'Breathing Problem', 'General Critical']
const hospitals = ['City Heart Emergency Institute', 'NeuroCare Trauma Center', 'Metro Burn and Critical Care', 'Highway Trauma Response Hospital']

export function HomePage() {
  return (
    <>
      <section className="hero-screen">
        <div className="hero-copy">
          <span className="status-pill">Emergency Operations Platform</span>
          <h1>Emergency Response When Every Second Counts</h1>
          <p>
            Coordinate patient requests, ambulance dispatch, hospital assignment, doctor assignment, beds, transfers, and notifications from one command surface.
          </p>
          <div className="hero-actions">
            <Link className="button primary" to="/emergency">
              Request Emergency Assistance
              <ArrowRight size={18} />
            </Link>
            <Link className="button secondary" to="/admission">
              Plan Hospital Admission
            </Link>
          </div>
          <div className="hero-stats">
            <strong>97%</strong>
            <span>cases routed with hospital and ambulance assignment</span>
            <strong>5-7</strong>
            <span>nearby hospitals generated per emergency</span>
          </div>
        </div>
        <OperationsPreview />
      </section>

      <section className="section-band">
        <div className="section-heading">
          <Route size={20} />
          <h2>Emergency Workflow</h2>
        </div>
        <div className="workflow-chain">
          {workflow.map((step, index) => (
            <div className="workflow-step" key={step}>
              <span>{index + 1}</span>
              <strong>{step}</strong>
              {index < workflow.length - 1 && <ArrowRight size={18} />}
            </div>
          ))}
        </div>
      </section>

      <section className="section-grid">
        <div>
          <div className="section-heading">
            <ShieldAlert size={20} />
            <h2>Emergency Types</h2>
          </div>
          <div className="tag-grid">
            {emergencyTypes.map((type) => (
              <span key={type}>{type}</span>
            ))}
          </div>
        </div>
        <div>
          <div className="section-heading">
            <Hospital size={20} />
            <h2>Hospital Network</h2>
          </div>
          <div className="list-panel">
            {hospitals.map((hospital) => (
              <div key={hospital}>
                <strong>{hospital}</strong>
                <span>Specialists, beds, ambulance dispatch</span>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="section-band stats-band">
        <div>
          <HeartPulse size={22} />
          <strong>Emergency metrics</strong>
          <span>Critical queue, dispatch ETAs, assignment status</span>
        </div>
        <div>
          <Bed size={22} />
          <strong>Admission metrics</strong>
          <span>Verification, review, doctor and bed allocation</span>
        </div>
        <footer>Smart Emergency Healthcare Operations Platform for Multi-Hospital Networks</footer>
      </section>
    </>
  )
}
