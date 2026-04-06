import { useQuery } from '@tanstack/react-query'
import { userApi, roleApi } from '../services/api'
import { useAuthStore } from '../store/authStore'
import { Users, ShieldCheck, Layers, Activity } from 'lucide-react'

export default function DashboardPage() {
  const { user } = useAuthStore()
  const isAdmin = user?.allRoles?.includes('ADMIN')

  const { data: usersData } = useQuery({ queryKey: ['users-summary'], queryFn: () => userApi.getAll({ page: 0, size: 1 }) })
  const { data: roles } = useQuery({ queryKey: ['roles-summary'], queryFn: roleApi.getAll, enabled: !!isAdmin })
  const { data: groups } = useQuery({ queryKey: ['groups-summary'], queryFn: roleApi.getAllGroups, enabled: !!isAdmin })

  const stats = [
    { label: 'Total Users', value: usersData?.totalElements ?? '—', icon: Users, color: 'bg-blue-500' },
    { label: 'Total Roles', value: isAdmin ? (roles?.length ?? '—') : 'N/A', icon: ShieldCheck, color: 'bg-green-500' },
    { label: 'Role Groups', value: isAdmin ? (groups?.length ?? '—') : 'N/A', icon: Layers, color: 'bg-purple-500' },
    { label: 'My Permissions', value: user?.allPermissions?.length ?? 0, icon: Activity, color: 'bg-orange-500' },
  ]

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Welcome back, {user?.firstName || user?.username}!</h1>
        <p className="text-gray-500 mt-1">Here's an overview of your system</p>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        {stats.map((stat) => (
          <div key={stat.label} className="card flex items-center gap-4">
            <div className={`w-12 h-12 ${stat.color} rounded-xl flex items-center justify-center flex-shrink-0`}>
              <stat.icon size={22} className="text-white" />
            </div>
            <div>
              <p className="text-2xl font-bold">{stat.value}</p>
              <p className="text-sm text-gray-500">{stat.label}</p>
            </div>
          </div>
        ))}
      </div>
      <div className="card">
        <h2 className="text-lg font-semibold mb-4">Your Access</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h3 className="text-sm font-medium text-gray-500 mb-2">Assigned Roles</h3>
            <div className="flex flex-wrap gap-2">
              {user?.allRoles?.length ? user.allRoles.map(r => (
                <span key={r} className="badge badge-blue">{r}</span>
              )) : <span className="text-gray-400 text-sm">No roles</span>}
            </div>
          </div>
          <div>
            <h3 className="text-sm font-medium text-gray-500 mb-2">Permissions</h3>
            <div className="flex flex-wrap gap-2">
              {user?.allPermissions?.slice(0,8).map(p => (
                <span key={p} className="badge badge-gray">{p}</span>
              ))}
              {(user?.allPermissions?.length ?? 0) > 8 && (
                <span className="badge badge-gray">+{(user?.allPermissions?.length ?? 0) - 8} more</span>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}