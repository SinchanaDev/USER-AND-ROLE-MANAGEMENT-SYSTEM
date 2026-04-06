import axios from 'axios'
import { useAuthStore } from '../store/authStore'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      const { refreshToken } = useAuthStore.getState()
      if (refreshToken) {
        try {
          const res = await axios.post(`/api/auth/refresh?refreshToken=${refreshToken}`)
          useAuthStore.getState().setAuth(res.data.user, res.data.accessToken, res.data.refreshToken)
          error.config.headers.Authorization = `Bearer ${res.data.accessToken}`
          return api(error.config)
        } catch {
          useAuthStore.getState().clearAuth()
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  login: (data: any) => api.post('/auth/login', data).then(r => r.data),
  changePassword: (data: any) => api.post('/auth/change-password', data),
  completeProfile: (data: any) => api.post('/auth/complete-profile', data),
  resetPassword: (data: any) => api.post('/auth/reset-password', data),
}

export const userApi = {
  getAll: (params: any) => api.get('/users', { params }).then(r => r.data),
  getById: (id: number) => api.get(`/users/${id}`).then(r => r.data),
  create: (data: any) => api.post('/users', data).then(r => r.data),
  update: (id: number, data: any) => api.put(`/users/${id}`, data).then(r => r.data),
  delete: (id: number) => api.delete(`/users/${id}`),
  toggleStatus: (id: number) => api.patch(`/users/${id}/toggle-status`),
  resetPassword: (id: number, newPassword: string) =>
    api.post(`/users/${id}/reset-password`, null, { params: { newPassword } }),
  exportExcel: () => api.get('/users/export/excel', { responseType: 'blob' }),
}

export const roleApi = {
  getAll: () => api.get('/roles').then(r => r.data),
  getById: (id: number) => api.get(`/roles/${id}`).then(r => r.data),
  create: (data: any) => api.post('/roles', data).then(r => r.data),
  update: (id: number, data: any) => api.put(`/roles/${id}`, data).then(r => r.data),
  delete: (id: number) => api.delete(`/roles/${id}`),
  getAllGroups: () => api.get('/roles/groups').then(r => r.data),
  createGroup: (data: any) => api.post('/roles/groups', data).then(r => r.data),
  updateGroup: (id: number, data: any) => api.put(`/roles/groups/${id}`, data).then(r => r.data),
  deleteGroup: (id: number) => api.delete(`/roles/groups/${id}`),
  getPermissions: () => api.get('/roles/permissions').then(r => r.data),
}

export default api