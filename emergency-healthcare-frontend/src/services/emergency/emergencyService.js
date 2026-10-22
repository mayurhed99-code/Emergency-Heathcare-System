import { apiClient } from '../api/client'

export const emergencyService = {
  nearbyHospitals(params) {
    return apiClient.get('/public/emergencies/nearby-hospitals', { params }).then((res) => res.data)
  },
  create(payload) {
    return apiClient.post('/public/emergencies', payload).then((res) => res.data)
  },
  track(trackingCode) {
    return apiClient.get(`/public/emergencies/tracking/${trackingCode}`).then((res) => res.data)
  },
}
