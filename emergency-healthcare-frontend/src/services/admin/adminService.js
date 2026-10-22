import { apiClient } from '../api/client'

export const adminService = {
  dashboard() {
    return apiClient.get('/admin/dashboard').then((res) => res.data)
  },
  hospitals() {
    return apiClient.get('/admin/hospitals').then((res) => res.data)
  },
  reports() {
    return apiClient.get('/admin/reports').then((res) => res.data)
  },
}
