import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { LayoutDashboard, Users, ShieldCheck, Layers, LogOut, User, Menu, X, ChevronDown } from 'lucide-react'
import { useState } from 'react'
import clsx from 'clsx'

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/users', icon: Users, label: 'Users' },
  { to: '/roles', icon: ShieldCheck, label: 'Roles', adminOnly: true },
  { to: '/role-groups', icon: Layers, label: 'Role Groups', adminOnly: true },
]

export default function Layout() {
  const { user, clearAuth } = useAuthStore()
  const navigate = useNavigate()
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [profileOpen, setProfileOpen] = useState(false)
  const isAdmin = user?.allRoles?.includes('ADMIN')

  const logout = () => { clearAuth(); navigate('/login') }

  return (
    <div className="flex h-screen overflow-hidden">
      <aside className={clsx('bg-gray-900 text-white flex flex-col transition-all duration-300 flex-shrink-0', sidebarOpen ? 'w-64' : 'w-16')}>
        <div className="flex items-center gap-3 p-4 border-b border-gray-700">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center flex-shrink-0">
            <ShieldCheck size={16} />
          </div>
          {sidebarOpen && <span className="font-bold text-sm truncate">UserMgmt System</span>}
        </div>
        <nav className="flex-1 py-4 space-y-1 px-2">
          {navItems.filter(i => !i.adminOnly || isAdmin).map(item => (
            <NavLink key={item.to} to={item.to}
              className={({ isActive }) => clsx(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                isActive ? 'bg-blue-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-white'
              )}>
              <item.icon size={18} className="flex-shrink-0" />
              {sidebarOpen && <span>{item.label}</span>}
            </NavLink>
          ))}
        </nav>
        <div className="p-3 border-t border-gray-700">
          {sidebarOpen ? (
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0">
                {user?.firstName?.[0] || user?.username?.[0]}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{user?.firstName || user?.username}</p>
                <p className="text-xs text-gray-400 truncate">{user?.email}</p>
              </div>
            </div>
          ) : (
            <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-xs font-bold mx-auto">
              {user?.firstName?.[0] || user?.username?.[0]}
            </div>
          )}
        </div>
      </aside>

      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between">
          <button onClick={() => setSidebarOpen(!sidebarOpen)} className="p-2 rounded-lg hover:bg-gray-100">
            {sidebarOpen ? <X size={18} /> : <Menu size={18} />}
          </button>
          <div className="flex items-center gap-3 relative">
            <button onClick={() => setProfileOpen(!profileOpen)}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-gray-100 text-sm">
              <User size={16} /><span>{user?.username}</span><ChevronDown size={14} />
            </button>
            {profileOpen && (
              <div className="absolute right-0 top-10 bg-white rounded-xl shadow-lg border border-gray-200 w-48 z-50">
                <NavLink to="/profile"
                  className="flex items-center gap-2 px-4 py-3 text-sm hover:bg-gray-50 rounded-t-xl"
                  onClick={() => setProfileOpen(false)}>
                  <User size={14} />My Profile
                </NavLink>
                <button onClick={logout}
                  className="w-full flex items-center gap-2 px-4 py-3 text-sm text-red-600 hover:bg-red-50 rounded-b-xl">
                  <LogOut size={14} />Sign Out
                </button>
              </div>
            )}
          </div>
        </header>
        <main className="flex-1 overflow-auto p-6"><Outlet /></main>
      </div>
    </div>
  )
}