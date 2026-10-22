import { Activity, Users, Truck, Bed, Clock, ArrowRightLeft, UserCheck } from 'lucide-react'

export function ResourceHealthPanel({ dashboard }) {
  const doctorsAvailable = dashboard?.doctors?.filter(d => d.available)?.length || 0
  const doctorsTotal = dashboard?.doctors?.length || 10
  
  const ambulancesAvailable = dashboard?.ambulances?.filter(a => a.status === 'AVAILABLE')?.length || 0
  const ambulancesTotal = dashboard?.ambulances?.length || 5
  
  // Fake total beds for now, assume 50 beds per hospital
  const bedsAvailable = dashboard?.beds?.length || 0
  const bedsTotal = 50

  const activeCases = dashboard?.activeEmergencies || 0
  const completedCases = dashboard?.completedCases || 0
  const transfers = dashboard?.transfers || 0
  
  // Fake response time for dashboard feel
  const avgResponseTime = activeCases > 0 ? '12m' : '8m'

  return (
    <div style={{ marginBottom: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
      {/* Daily Statistics Strip */}
      <div className="daily-stats-strip" style={{ 
        display: 'flex', flexWrap: 'wrap', gap: '12px', background: 'var(--bg-elevated)', 
        padding: '12px 16px', borderRadius: '8px', border: '1px solid var(--border)',
        alignItems: 'center', justifyContent: 'space-between'
      }}>
        <StatItem icon={<Activity size={16} />} label="Active Cases" value={activeCases} highlight={activeCases > 0} />
        <div style={{ width: '1px', height: '24px', background: 'var(--border)' }}></div>
        <StatItem icon={<UserCheck size={16} />} label="Completed" value={completedCases} />
        <div style={{ width: '1px', height: '24px', background: 'var(--border)' }}></div>
        <StatItem icon={<ArrowRightLeft size={16} />} label="Transfers" value={transfers} />
        <div style={{ width: '1px', height: '24px', background: 'var(--border)' }}></div>
        <StatItem icon={<Clock size={16} />} label="Avg Response" value={avgResponseTime} />
      </div>

      {/* KPI Resource Utilization Cards */}
      <div className="ops-metrics" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
        <UtilizationCard 
          icon={<Users size={20} />} 
          label="Doctors on Duty" 
          available={doctorsAvailable} 
          total={doctorsTotal} 
        />
        <UtilizationCard 
          icon={<Truck size={20} />} 
          label="Ambulances Ready" 
          available={ambulancesAvailable} 
          total={ambulancesTotal} 
        />
        <UtilizationCard 
          icon={<Bed size={20} />} 
          label="ICU/Trauma Beds" 
          available={bedsAvailable} 
          total={bedsTotal} 
        />
      </div>
    </div>
  )
}

function StatItem({ icon, label, value, highlight }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
      <div style={{ color: highlight ? 'var(--critical)' : 'var(--muted)', display: 'flex' }}>{icon}</div>
      <div style={{ display: 'flex', flexDirection: 'column' }}>
        <span style={{ fontSize: '0.7rem', textTransform: 'uppercase', color: 'var(--muted)', letterSpacing: '0.5px' }}>{label}</span>
        <strong style={{ fontSize: '1rem', color: highlight ? 'var(--critical)' : 'var(--text-primary)', lineHeight: 1 }}>{value}</strong>
      </div>
    </div>
  )
}

function UtilizationCard({ icon, label, available, total }) {
  const percent = total > 0 ? (available / total) * 100 : 0
  
  let indicatorColor = 'var(--success)' // Green
  let statusText = 'Healthy'
  
  if (percent <= 30) {
    indicatorColor = 'var(--critical)' // Red
    statusText = 'Critical'
  } else if (percent <= 60) {
    indicatorColor = 'var(--warning)' // Yellow
    statusText = 'Warning'
  }

  return (
    <div className="metric-card" style={{ 
      background: 'var(--bg-elevated)', 
      border: '1px solid var(--border)',
      padding: '16px',
      borderRadius: '8px',
      display: 'flex',
      flexDirection: 'column',
      gap: '12px'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center', color: 'var(--muted)' }}>
          {icon}
          <h3 style={{ fontSize: '0.85rem', margin: 0, fontWeight: 500 }}>{label}</h3>
        </div>
        <span style={{ fontSize: '0.7rem', padding: '2px 6px', borderRadius: '4px', background: `${indicatorColor}20`, color: indicatorColor, fontWeight: 'bold' }}>
          {statusText}
        </span>
      </div>
      
      <div style={{ display: 'flex', alignItems: 'baseline', gap: '4px' }}>
        <span style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--text-primary)', lineHeight: 1 }}>{available}</span>
        <span style={{ fontSize: '0.9rem', color: 'var(--muted)' }}>/ {total} Available</span>
      </div>
      
      <div style={{ width: '100%', height: '6px', background: 'var(--bg-hover)', borderRadius: '3px', overflow: 'hidden' }}>
        <div style={{ width: `${percent}%`, height: '100%', background: indicatorColor, transition: 'width 0.5s ease-out' }}></div>
      </div>
    </div>
  )
}
