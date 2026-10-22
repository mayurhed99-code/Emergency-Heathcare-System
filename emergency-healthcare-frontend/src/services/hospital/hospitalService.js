import { apiClient } from '../api/client'

export const hospitalService = {
  dashboard(hospitalId = 1) {
    return apiClient.get('/hospital/dashboard', { params: { hospitalId } }).then((res) => res.data)
  },
  acceptEmergency(code) {
    return apiClient.post(`/hospital/emergencies/${code}/accept`).then((res) => res.data)
  },
  assignDoctor(code, doctorId = null) {
    return apiClient.post(`/hospital/emergencies/${code}/assign-doctor`, { doctorId }).then((res) => res.data)
  },
  allocateBed(code, bedId = null) {
    return apiClient.post(`/hospital/emergencies/${code}/allocate-bed`, { bedId }).then((res) => res.data)
  },
  approveAdmission(code) {
    return apiClient.post(`/hospital/admissions/${code}/approve`).then((res) => res.data)
  },
  assignAdmissionDoctor(code, doctorId = null) {
    return apiClient.post(`/hospital/admissions/${code}/assign-doctor`, { doctorId }).then((res) => res.data)
  },
  allocateAdmissionBed(code, bedId = null) {
    return apiClient.post(`/hospital/admissions/${code}/allocate-bed`, { bedId }).then((res) => res.data)
  },
  transferPatient(code, payload) {
    return apiClient.post(`/hospital/emergencies/${code}/transfer`, payload).then((res) => res.data)
  },
  updateStatus(code, status) {
    return apiClient.patch(`/hospital/emergencies/${code}/status`, null, { params: { status } }).then((res) => res.data)
  },
  sendNotification(payload) {
    return apiClient.post('/hospital/notifications/send', payload).then((res) => res.data)
  },
  getDoctors() {
    return apiClient.get('/hospital/resources/doctors').then((res) => res.data)
  },
  addDoctor(payload) {
    return apiClient.post('/hospital/resources/doctors', payload).then((res) => res.data)
  },
  getDrivers() {
    return apiClient.get('/hospital/resources/drivers').then((res) => res.data)
  },
  addDriver(payload) {
    return apiClient.post('/hospital/resources/drivers', payload).then((res) => res.data)
  },
  getAmbulances() {
    return apiClient.get('/hospital/resources/ambulances').then((res) => res.data)
  },
  addAmbulance(payload) {
    return apiClient.post('/hospital/resources/ambulances', payload).then((res) => res.data)
  },
}
