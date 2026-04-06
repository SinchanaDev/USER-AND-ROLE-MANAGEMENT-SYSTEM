import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { roleApi } from '../services/api'
import toast from 'react-hot-toast'
import { Plus, Edit, Trash2, X, ShieldCheck } from 'lucide-react'
import { useForm } from 'react-hook-form'

function RoleModal({ role, permissions, onClose }: any) {
  const qc = useQueryClient()
  const isEdit = !!role
  const { register, handleSubmit } = useForm({
    defaultValues: role ? { ...role, permissionIds: role.permissions?.map((p: any) => p.id) || [] } : {}
  })
  const mutation = useMutation({
    mutationFn: (data: any) => isEdit ? roleApi.update(role.id, data) : roleApi.create(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['roles'] }); toast.success(`Role ${isEdit ? 'updated' : 'created'}`); onClose() },
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed')
  })
  const byModule: Record<string, any[]> = {}
  permissions?.forEach((p: any) => { if (!byModule[p.module]) byModule[p.module] = []; byModule[p.module].push(p) })
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl w-full max-w-lg shadow-2xl">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-lg font-semibold">{isEdit ? 'Edit Role' : 'Create Role'}</h2>
          <button onClick={onClose}><X size={20} className="text-gray-400" /></button>
        </div>
        <form onSubmit={handleSubmit(d => mutation.mutate(d))} className="p-6 space-y-4">
          <div><label className="label">Role Name *</label><input {...register('name', { required: true })} className="input" placeholder="e.g. REPORTER" /></div>
          <div><label className="label">Description</label><textarea {...register('description')} className="input" rows={2} /></div>
          <div>
            <label className="label">Permissions</label>
            <div className="border border-gray-200 rounded-lg p-3 max-h-48 overflow-y-auto space-y-3">
              {Object.entries(byModule).map(([module, perms]) => (
                <div key={module}>
                  <p className="text-xs font-bold text-gray-500 uppercase mb-1">{module}</p>
                  {perms.map((p: any) => (
                    <label key={p.id} className="flex items-center gap-2 cursor-pointer mb-1">
                      <input type="checkbox" value={p.id} {...register('permissionIds')} className="rounded" />
                      <span className="text-sm">{p.name}</span>
                      <span className="text-xs text-gray-400">— {p.description}</span>
                    </label>
                  ))}
                </div>
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

export default function RolesPage() {
  const qc = useQueryClient()
  const [modal, setModal] = useState<any>(null)
  const { data: roles, isLoading } = useQuery({ queryKey: ['roles'], queryFn: roleApi.getAll })
  const { data: permissions } = useQuery({ queryKey: ['permissions'], queryFn: roleApi.getPermissions })
  const deleteMutation = useMutation({
    mutationFn: roleApi.delete,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['roles'] }); toast.success('Role deleted') },
    onError: (err: any) => toast.error(err.response?.data?.message || 'Cannot delete')
  })
  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div><h1 className="text-2xl font-bold">Roles</h1><p className="text-gray-500 text-sm mt-1">Define roles and permissions</p></div>
        <button onClick={() => setModal({ type: 'role' })} className="btn-primary flex items-center gap-2"><Plus size={16} />Add Role</button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {isLoading ? <p className="text-gray-500 col-span-3 py-8 text-center">Loading...</p> : roles?.map((role: any) => (
          <div key={role.id} className="card">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="w-9 h-9 bg-blue-100 text-blue-700 rounded-lg flex items-center justify-center"><ShieldCheck size={18} /></div>
                <div><p className="font-semibold">{role.name}</p>{role.systemRole && <span className="badge badge-blue text-xs">System</span>}</div>
              </div>
              {!role.systemRole && (
                <div className="flex gap-1">
                  <button onClick={() => setModal({ type: 'role', data: role })} className="p-1.5 rounded hover:bg-blue-50 text-blue-600"><Edit size={14} /></button>
                  <button onClick={() => confirm('Delete role?') && deleteMutation.mutate(role.id)} className="p-1.5 rounded hover:bg-red-50 text-red-500"><Trash2 size={14} /></button>
                </div>
              )}
            </div>
            <p className="text-sm text-gray-500 mb-3">{role.description || 'No description'}</p>
            <div className="flex flex-wrap gap-1">
              {role.permissions?.slice(0,4).map((p: any) => <span key={p.id} className="badge badge-gray">{p.name}</span>)}
              {role.permissions?.length > 4 && <span className="badge badge-gray">+{role.permissions.length-4}</span>}
            </div>
          </div>
        ))}
      </div>
      {modal?.type==='role' && <RoleModal role={modal.data} permissions={permissions} onClose={() => setModal(null)} />}
    </div>
  )
}