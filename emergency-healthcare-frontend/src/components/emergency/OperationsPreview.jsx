import { Ambulance, Building2, RadioTower, Siren } from 'lucide-react'
import { MetricCard } from '../common/MetricCard.jsx'

const dispatchEvents = [
  'Heart attack case routed to Cardiology',
  'AMB-CARDIO dispatched, ETA 6 min',
  'ICU bed reserved for EMG-83A91C',
  'Driver alert acknowledged',
]

export function OperationsPreview() {
  return (
    <section className="ops-preview" aria-label="Emergency operations preview">
      <div className="ops-grid-head">
        <span className="status-pill live">LIVE COMMAND</span>
        <span className="muted">Multi-hospital coordination grid</span>
      </div>
      <div className="ops-metrics">
        <MetricCard label="Hospitals" value="6" />
        <MetricCard label="Ambulances" value="43" tone="warning" />
        <MetricCard label="Emergency Cases" value="18" tone="critical" />
        <MetricCard label="Dispatch Events" value="124" tone="success" />
      </div>
      <div className="command-board">
        <div className="node hospital-node">
          <Building2 size={18} />
          City Heart
          <span>Cardiology ready</span>
        </div>
        <div className="node ambulance-node">
          <Ambulance size={18} />
          AMB-CARDIO
          <span>ETA 6 min</span>
        </div>
        <div className="node case-node">
          <Siren size={18} />
          EMG-83A91C
          <span>Critical</span>
        </div>
        <div className="node dispatch-node">
          <RadioTower size={18} />
          Dispatch
          <span>Alerts sent</span>
        </div>
      </div>
      <ul className="event-feed">
        {dispatchEvents.map((event) => (
          <li key={event}>
            <span />
            {event}
          </li>
        ))}
      </ul>
    </section>
  )
}
