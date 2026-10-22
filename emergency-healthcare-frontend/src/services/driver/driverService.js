import { apiClient } from '../api/client'

export const driverService = {
  assignments() {
    return apiClient.get('/driver/assignments').then((res) => res.data)
  },
}
