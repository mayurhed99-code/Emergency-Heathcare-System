import { apiClient } from '../api/client'

export const doctorService = {
  cases() {
    return apiClient.get('/doctor/cases').then((res) => res.data)
  },
  updateStatus(code, status) {
    return apiClient.patch(`/doctor/cases/${code}/status`, null, { params: { status } }).then((res) => res.data)
  },
}
