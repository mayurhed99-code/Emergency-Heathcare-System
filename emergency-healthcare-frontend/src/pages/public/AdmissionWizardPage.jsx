import { useState } from 'react'
import { CheckCircle2, Mail, MessageSquareText, Send } from 'lucide-react'
import { apiError } from '../../services/api/client'
import { admissionService } from '../../services/admission/admissionService'

const steps = ['Patient Details', 'Symptoms', 'Specialization', 'Verification', 'OTP', 'Submitted', 'Hospital Review', 'Doctor', 'Bed', 'PDF']

const initialAdmission = {
  name: '',
  phone: '',
  email: '',
  symptoms: '',
  preferredSpecialization: 'CARDIOLOGY',
  verificationMethod: 'SMS',
  otp: '',
  locationText: 'Bengaluru',
}

export function AdmissionWizardPage() {
  const [step, setStep] = useState(0)
  const [form, setForm] = useState(initialAdmission)
  const [result, setResult] = useState(null)
  const [demoOtp, setDemoOtp] = useState('')
  const [error, setError] = useState('')

  function updateField(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function requestOtp() {
    setError('')
    try {
      const target = form.verificationMethod === 'SMS' ? form.phone : form.email
      const response = await admissionService.startOtp(target)
      setDemoOtp(response.demoOtp)
      setStep(4)
    } catch (requestError) {
      setError(apiError(requestError))
    }
  }

  async function submitAdmission() {
    setError('')
    try {
      const response = await admissionService.submit(form)
      setResult(response)
      setStep(5)
    } catch (requestError) {
      setError(apiError(requestError))
    }
  }

  return (
    <section className="wizard-screen">
      <div className="wizard-rail">
        {steps.map((item, index) => (
          <button key={item} className={index <= step ? 'active' : ''} onClick={() => setStep(index)} type="button">
            <span>{index + 1}</span>
            {item}
          </button>
        ))}
      </div>

      <div className="workflow-panel wizard-panel">
        <span className="status-pill">Planned Admission</span>
        <h1>{steps[step]}</h1>
        {error && <div className="alert critical">{error}</div>}

        {step === 0 && (
          <div className="ops-form">
            <label>Patient Name<input name="name" value={form.name} onChange={updateField} /></label>
            <label>Phone<input name="phone" value={form.phone} onChange={updateField} /></label>
            <label>Email<input name="email" value={form.email} onChange={updateField} /></label>
            <button className="button primary" onClick={() => setStep(1)} type="button">Continue</button>
          </div>
        )}
        {step === 1 && (
          <div className="ops-form">
            <label>Symptoms<textarea name="symptoms" value={form.symptoms} onChange={updateField} rows={5} /></label>
            <button className="button primary" onClick={() => setStep(2)} type="button">Continue</button>
          </div>
        )}
        {step === 2 && (
          <div className="ops-form">
            <label>
              Preferred Specialization
              <select name="preferredSpecialization" value={form.preferredSpecialization} onChange={updateField}>
                <option value="CARDIOLOGY">Cardiology</option>
                <option value="NEUROLOGY">Neurology</option>
                <option value="BURN_UNIT">Burn Unit</option>
                <option value="TRAUMA_CENTER">Trauma Center</option>
                <option value="ICU">ICU</option>
              </select>
            </label>
            <button className="button primary" onClick={() => setStep(3)} type="button">Continue</button>
          </div>
        )}
        {step === 3 && (
          <div className="choice-row">
            <button className={form.verificationMethod === 'SMS' ? 'choice active' : 'choice'} onClick={() => setForm({ ...form, verificationMethod: 'SMS' })} type="button">
              <MessageSquareText size={18} />
              SMS OTP
            </button>
            <button className={form.verificationMethod === 'EMAIL' ? 'choice active' : 'choice'} onClick={() => setForm({ ...form, verificationMethod: 'EMAIL' })} type="button">
              <Mail size={18} />
              Email OTP
            </button>
            <button className="button primary" onClick={requestOtp} type="button">
              <Send size={17} />
              Send OTP
            </button>
          </div>
        )}
        {step === 4 && (
          <div className="ops-form">
            {demoOtp && <div className="alert">Demo OTP: {demoOtp}</div>}
            <label>OTP<input name="otp" value={form.otp} onChange={updateField} /></label>
            <button className="button primary" onClick={submitAdmission} type="button">Submit Admission Request</button>
          </div>
        )}
        {step >= 5 && (
          <div className="confirmation-panel">
            <CheckCircle2 size={42} />
            <h2>{result?.trackingCode || 'Admission Request Submitted'}</h2>
            <p>{result?.hospital || 'Hospital review queue'}</p>
            <div className="workflow-chain compact">
              {steps.slice(5).map((item, index) => (
                <div className={index <= step - 5 ? 'workflow-step done' : 'workflow-step'} key={item}>
                  <span>{index + 6}</span>
                  <strong>{item}</strong>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </section>
  )
}
