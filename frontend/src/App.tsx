import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout from './components/layout/Layout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import UsersPage from './pages/UsersPage'
import RolesPage from './pages/RolesPage'
import RoleGroupsPage from './pages/RoleGroupsPage'
import ProfilePage from './pages/ProfilePage'
import CompleteProfilePage from './pages/CompleteProfilePage'
import ResetPasswordPage from './pages/ResetPasswordPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { accessToken, user } = useAuthStore()
  if (!accessToken) return <Navigate to="/login" replace />
  if (user?.firstLogin) return <Navigate to="/complete-profile" replace />
  return <>{children}</>
}

function AdminRoute({ children }: { children: React.ReactNode }) {
  const { accessToken, user } = useAuthStore()
  if (!accessToken) return <Navigate to="/login" replace />
  if (!user?.allRoles?.includes('ADMIN')) return <Navigate to="/dashboard" replace />
  return <>{children}</>
}

export default function App() {
  const { accessToken } = useAuthStore()
  return (
    <Routes>
      <Route path="/login" element={!accessToken ? <LoginPage /> : <Navigate to="/dashboard" replace />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />
      <Route path="/complete-profile" element={<CompleteProfilePage />} />
      <Route path="/" element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="roles" element={<AdminRoute><RolesPage /></AdminRoute>} />
        <Route path="role-groups" element={<AdminRoute><RoleGroupsPage /></AdminRoute>} />
        <Route path="profile" element={<ProfilePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}