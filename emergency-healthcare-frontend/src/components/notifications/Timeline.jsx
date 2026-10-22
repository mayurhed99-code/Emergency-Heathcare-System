export function Timeline({ items = [] }) {
  if (!items.length) {
    return <div className="empty-state">No timeline events yet.</div>
  }

  return (
    <ol className="timeline">
      {items.map((item, index) => (
        <li key={`${item.label}-${index}`}>
          <span className="timeline-dot" />
          <div>
            <strong>{item.label}</strong>
            <p>{item.detail}</p>
          </div>
          <time>{item.createdAt ? new Date(item.createdAt).toLocaleString() : 'Pending'}</time>
        </li>
      ))}
    </ol>
  )
}
