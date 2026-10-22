import { apiClient } from '../api/client'

export const admissionService = {
  startOtp(target) {
    return apiClient.post('/public/admissions/otp', null, { params: { target } }).then((res) => res.data)
  },
  submit(payload) {
    return apiClient.post('/public/admissions', payload).then((res) => res.data)
  },
}
