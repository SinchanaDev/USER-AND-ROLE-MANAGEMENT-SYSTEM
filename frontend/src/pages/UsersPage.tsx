import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { userApi, roleApi } from '../services/api'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'
import { Plus, Search, Edit, Trash2, Lock, Unlock, RefreshCw, Download, X } from 'lucide-react'
import { useForm } from 'react-hook-form'

const PASSWORD_RULES = {
  required: 'Password is required',
  minLength: { value: 8, message: 'Minimum 8 characters' },
  pattern: {
    value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])/,
    message: 'Must include uppercase, lowercase, number and special character'
  }
}

function UserModal({ user, onClose, roleGroups }: any) {
  const qc = useQueryClient()
  const isEdit = !!user
  const { register, handleSubmit } = useForm({
    defaultValues: user ? { ...user, roleGroupIds: user.roleGroups?.map((g: any) => g.id) || [] } : { active: true }
  })
  const mutation = useMutation({
    mutationFn: (data: any) => isEdit ? userApi.update(user.id, data) : userApi.create(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }); toast.success(`User ${isEdit ? 'updated' : 'created'}`); onClose() },
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed')
  })
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl w-full max-w-lg shadow-2xl">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-lg font-semibold">{isEdit ? 'Edit User' : 'Create User'}</h2>
          <button onClick={onClose}><X size={20} className="text-gray-400" /></button>
        </div>
        <form onSubmit={handleSubmit(d => mutation.mutate(d))} className="p-6 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div><label className="label">Username *</label><input {...register('username', { required: true })} className="input" disabled={isEdit} /></div>
            <div><label className="label">Email *</label><input {...register('email', { required: true })} type="email" className="input" /></div>
            <div><label className="label">First Name</label><input {...register('firstName')} className="input" /></div>
            <div><label className="label">Last Name</label><input {...register('lastName')} className="input" /></div>
            {!isEdit && <div className="col-span-2"><label className="label">Password *</label><input {...register('password', PASSWORD_RULES)} type="password" className="input" placeholder="Min 8 chars, uppercase, digit, special char" /></div>}
            <div><label className="label">Phone</label><input {...register('phoneNumber')} className="input" /></div>
            <div><label className="label">Theme</label>
              <select {...register('profileTheme')} className="input">
                <option value="DEFAULT">Default</option>
                <option value="DARK">Dark</option>
                <option value="BLUE">Blue</option>
              </select>
            </div>
          </div>
          <div>
            <label className="label">Role Groups</label>
            <div className="border border-gray-200 rounded-lg p-3 max-h-32 overflow-y-auto space-y-2">
              {roleGroups?.map((g: any) => (
                <label key={g.id} className="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" value={g.id} {...register('roleGroupIds')} className="rounded" />
                  <span className="text-sm">{g.name}</span>
                </label>
              ))}
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={mutation.isPending} className="btn-primary">{mutation.isPending ? 'Saving...' : isEdit ? 'Update' : 'Create'}</button>
          </div>
        </form>
      </div>
    </div>
  )
}

function ResetModal({ userId, onClose }: any) {
  const { register, handleSubmit, formState: { errors } } = useForm()
  const mutation = useMutation({
    mutationFn: ({ password }: any) => userApi.resetPassword(userId, password),
    onSuccess: () => { toast.success('Password reset'); onClose() },
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed')
  })
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl w-full max-w-sm shadow-2xl">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-lg font-semibold">Reset Password</h2>
          <button onClick={onClose}><X size={20} className="text-gray-400" /></button>
        </div>
        <form onSubmit={handleSubmit(d => mutation.mutate(d))} className="p-6 space-y-4">
          <div>
            <label className="label">New Password</label>
            <input {...register('password', PASSWORD_RULES)} type="password" className="input" placeholder="Min 8 chars, uppercase, digit, special char" />
            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password.message as string}</p>}
          </div>
          <div className="p-3 bg-yellow-50 rounded-lg text-xs text-yellow-700">
            Password must contain: uppercase, lowercase, number and special character (!@#$%^&*)
          </div>
          <div className="flex justify-end gap-3">
            <button type="button" onClick={onClose} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={mutation.isPending} className="btn-primary">Reset</button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function UsersPage() {
  const { user: currentUser } = useAuthStore()
  const isAdmin = currentUser?.allRoles?.includes('ADMIN')
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [modal, setModal] = useState<any>(null)

  const { data, isLoading } = useQuery({ queryKey: ['users', search, page], queryFn: () => userApi.getAll({ search, page, size: 10 }) })
  const { data: roleGroups } = useQuery({ queryKey: ['role-groups'], queryFn: roleApi.getAllGroups })

  const deleteMutation = useMutation({
    mutationFn: userApi.delete,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }); toast.success('User deleted') },
    onError: () => toast.error('Delete failed')
  })
  const toggleMutation = useMutation({
    mutationFn: userApi.toggleStatus,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }); toast.success('Status updated') }
  })

  const exportExcel = async () => {
    try {
      const res = await userApi.exportExcel()
      const url = window.URL.createObjectURL(res.data)
      const a = document.createElement('a'); a.href = url; a.download = 'users.xlsx'; a.click()
      window.URL.revokeObjectURL(url)
    } catch { toast.error('Export failed') }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div><h1 className="text-2xl font-bold">Users</h1><p className="text-gray-500 text-sm mt-1">Manage system users</p></div>
        <div className="flex gap-2">
          {isAdmin && <button onClick={exportExcel} className="btn-secondary flex items-center gap-2"><Download size={16} />Export</button>}
          <button onClick={() => setModal({ type: 'user' })} className="btn-primary flex items-center gap-2"><Plus size={16} />Add User</button>
        </div>
      </div>
      <div className="card">
        <div className="flex items-center gap-3 mb-4">
          <div className="relative flex-1 max-w-sm">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input value={search} onChange={e => { setSearch(e.target.value); setPage(0) }} placeholder="Search users..." className="input pl-9" />
          </div>
          <span className="text-sm text-gray-500">{data?.totalElements ?? 0} users</span>
        </div>
        {isLoading ? <div className="text-center py-12 text-gray-500">Loading...</div> : (
          <div className="overflow-x-auto">
            <table className="table">
              <thead><tr><th>User</th><th>Email</th><th>Roles</th><th>Status</th><th>Created</th><th>Actions</th></tr></thead>
              <tbody>
                {data?.content?.map((u: any) => (
                  <tr key={u.id}>
                    <td>
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-blue-100 text-blue-700 rounded-full flex items-center justify-center text-xs font-bold">
                          {u.firstName?.[0] || u.username[0]}
                        </div>
                        <div><p className="font-medium">{u.username}</p><p className="text-xs text-gray-400">{u.firstName} {u.lastName}</p></div>
                      </div>
                    </td>
                    <td className="text-gray-600">{u.email}</td>
                    <td><div className="flex flex-wrap gap-1">{u.allRoles?.slice(0,2).map((r: string) => <span key={r} className="badge badge-blue">{r}</span>)}</div></td>
                    <td><span className={u.active ? 'badge-green badge' : 'badge-red badge'}>{u.active ? 'Active' : 'Inactive'}</span></td>
                    <td className="text-gray-500 text-xs">{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}</td>
                    <td>
                      <div className="flex items-center gap-1">
                        <button onClick={() => setModal({ type: 'user', data: u })} className="p-1.5 rounded hover:bg-blue-50 text-blue-600" title="Edit"><Edit size={14} /></button>
                        <button onClick={() => setModal({ type: 'reset', data: u })} className="p-1.5 rounded hover:bg-yellow-50 text-yellow-600" title="Reset Password"><RefreshCw size={14} /></button>
                        <button onClick={() => toggleMutation.mutate(u.id)} className={`p-1.5 rounded ${u.active ? 'hover:bg-red-50 text-red-500' : 'hover:bg-green-50 text-green-500'}`}>{u.active ? <Lock size={14} /> : <Unlock size={14} />}</button>
                        {isAdmin && <button onClick={() => confirm('Delete user?') && deleteMutation.mutate(u.id)} className="p-1.5 rounded hover:bg-red-50 text-red-500"><Trash2 size={14} /></button>}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between mt-4 pt-4 border-t">
            <span className="text-sm text-gray-500">Page {page+1} of {data.totalPages}</span>
            <div className="flex gap-2">
              <button onClick={() => setPage(p => Math.max(0,p-1))} disabled={page===0} className="btn-secondary btn-sm">Previous</button>
              <button onClick={() => setPage(p => Math.min(data.totalPages-1,p+1))} disabled={page>=data.totalPages-1} className="btn-secondary btn-sm">Next</button>
            </div>
          </div>
        )}
      </div>
      {modal?.type==='user' && <UserModal user={modal.data} roleGroups={roleGroups} onClose={() => setModal(null)} />}
      {modal?.type==='reset' && <ResetModal userId={modal.data?.id} onClose={() => setModal(null)} />}
    </div>
  )
}