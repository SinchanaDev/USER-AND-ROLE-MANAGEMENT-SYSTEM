import { useState } from 'react'
import { useAuthStore } from '../store/authStore'
import { authApi } from '../services/api'
import { useForm } from 'react-hook-form'
import toast from 'react-hot-toast'
import { Eye, EyeOff } from 'lucide-react'

export default function ProfilePage() {
  const { user } = useAuthStore()
  const [showPass, setShowPass] = useState(false)
  const [tab, setTab] = useState<'info'|'password'>('info')
  const { register, handleSubmit, reset } = useForm()

  const onChangePassword = async (data: any) => {
    if (data.newPassword !== data.confirmPassword) { toast.error('Passwords do not match'); return }
    try {
      await authApi.changePassword(data)
      toast.success('Password changed!')
      reset()
    } catch (err: any) { toast.error(err.response?.data?.message || 'Failed') }
  }

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold mb-6">My Profile</h1>
      <div className="card">
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 bg-blue-600 text-white rounded-2xl flex items-center justify-center text-2xl font-bold">
            {user?.firstName?.[0] || user?.username?.[0]}
          </div>
          <div>
            <h2 className="text-lg font-semibold">{user?.firstName} {user?.lastName}</h2>
            <p className="text-gray-500">{user?.email}</p>
            <div className="flex gap-1 mt-1">{user?.allRoles?.map(r => <span key={r} className="badge badge-blue">{r}</span>)}</div>
          </div>
        </div>
        <div className="flex border-b mb-4">
          {(['info','password'] as const).map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`px-4 py-2 text-sm font-medium border-b-2 transition-colors ${tab===t ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500'}`}>
              {t==='info' ? 'Account Info' : 'Change Password'}
            </button>
          ))}
        </div>
        {tab==='info' && (
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div><span className="text-gray-500">Username:</span><span className="font-medium ml-2">{user?.username}</span></div>
            <div><span className="text-gray-500">Email:</span><span className="font-medium ml-2">{user?.email}</span></div>
            <div><span className="text-gray-500">Theme:</span><span className="font-medium ml-2">{user?.profileTheme}</span></div>
            <div><span className="text-gray-500">Permissions:</span><span className="font-medium ml-2">{user?.allPermissions?.length ?? 0}</span></div>
          </div>
        )}
        {tab==='password' && (
          <form onSubmit={handleSubmit(onChangePassword)} className="space-y-4">
            <div><label className="label">Current Password</label><input {...register('currentPassword',{required:true})} type="password" className="input" /></div>
            <div>
              <label className="label">New Password</label>
              <div className="relative">
                <input {...register('newPassword',{required:true,minLength:8})} type={showPass?'text':'password'} className="input pr-10" />
                <button type="button" onClick={() => setShowPass(!showPass)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
                  {showPass ? <EyeOff size={16}/> : <Eye size={16}/>}
                </button>
              </div>
              <p className="text-xs text-gray-400 mt-1">Min 8 chars, uppercase, lowercase, digit, special char</p>
            </div>
            <div><label className="label">Confirm Password</label><input {...register('confirmPassword',{required:true})} type="password" className="input" /></div>
            <button type="submit" className="btn-primary">Update Password</button>
          </form>
        )}
      </div>
    </div>
  )
}