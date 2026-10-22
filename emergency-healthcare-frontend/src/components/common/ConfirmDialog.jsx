import { Modal } from './Modal.jsx'

export function ConfirmDialog({ isOpen, title, message, onConfirm, onCancel }) {
  return (
    <Modal isOpen={isOpen} onClose={onCancel} title={title}>
      <div style={{ marginBottom: '24px', color: 'var(--text-secondary)' }}>
        {message}
      </div>
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
        <button className="button ghost" onClick={onCancel}>Cancel</button>
        <button className="button primary" onClick={onConfirm} style={{ background: 'var(--critical)' }}>Confirm</button>
      </div>
    </Modal>
  )
}
