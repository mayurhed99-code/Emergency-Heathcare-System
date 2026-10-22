import { apiClient } from '../api/client'

export const authService = {
  login(payload) {
    return apiClient.post('/auth/login', payload).then((res) => res.data)
  },
  verifyDriverOtp(payload) {
    return apiClient.post('/auth/driver/verify-otp', payload).then((res) => res.data)
  },
}
