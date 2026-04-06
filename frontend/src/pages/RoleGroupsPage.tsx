import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { roleApi } from '../services/api'
import toast from 'react-hot-toast'
import { Plus, Edit, Trash2, X, Layers, Users } from 'lucide-react'
import { useForm } from 'react-hook-form'

function GroupModal({ group, roles, onClose }: any) {
  const qc = useQueryClient()
  const isEdit = !!group
  const { register, handleSubmit } = useForm({
    defaultValues: group ? { ...group, roleIds: group.roles?.map((r: any) => r.id) || [] } : {}
  })
  const mutation = useMutation({
    mutationFn: (data: any) => isEdit ? roleApi.updateGroup(group.id, data) : roleApi.createGroup(data),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['role-groups'] }); toast.success(`Group ${isEdit ? 'updated' : 'created'}`); onClose() },
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed')
  })
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl w-full max-w-lg shadow-2xl">
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-lg font-semibold">{isEdit ? 'Edit Group' : 'Create Group'}</h2>
          <button onClick={onClose}><X size={20} className="text-gray-400" /></button>
        </div>
        <form onSubmit={handleSubmit(d => mutation.mutate(d))} className="p-6 space-y-4">
          <div><label className="label">Group Name *</label><input {...register('name', { required: true })} className="input" /></div>
          <div><label className="label">Description</label><textarea {...register('description')} className="input" rows={2} /></div>
          <div>
            <label className="label">Assign Roles</label>
            <div className="border border-gray-200 rounded-lg p-3 max-h-40 overflow-y-auto space-y-2">
              {roles?.map((r: any) => (
                <label key={r.id} className="flex items-center gap-2 cursor-pointer">
                  <input type="checkbox" value={r.id} {...register('roleIds')} className="rounded" />
                  <span className="text-sm font-medium">{r.name}</span>
                  {r.systemRole && <span className="badge badge-blue text-xs">System</span>}
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

export default function RoleGroupsPage() {
  const qc = useQueryClient()
  const [modal, setModal] = useState<any>(null)
  const { data: groups, isLoading } = useQuery({ queryKey: ['role-groups'], queryFn: roleApi.getAllGroups })
  const { data: roles } = useQuery({ queryKey: ['roles'], queryFn: roleApi.getAll })
  const deleteMutation = useMutation({
    mutationFn: roleApi.deleteGroup,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['role-groups'] }); toast.success('Group deleted') },
    onError: (err: any) => toast.error(err.response?.data?.message || 'Failed')
  })
  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div><h1 className="text-2xl font-bold">Role Groups</h1><p className="text-gray-500 text-sm mt-1">Bundle roles for easy assignment</p></div>
        <button onClick={() => setModal({ type: 'group' })} className="btn-primary flex items-center gap-2"><Plus size={16} />Add Group</button>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {isLoading ? <p className="text-gray-500 col-span-3 py-8 text-center">Loading...</p> : groups?.map((group: any) => (
          <div key={group.id} className="card">
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="w-9 h-9 bg-purple-100 text-purple-700 rounded-lg flex items-center justify-center"><Layers size={18} /></div>
                <p className="font-semibold">{group.name}</p>
              </div>
              <div className="flex gap-1">
                <button onClick={() => setModal({ type: 'group', data: group })} className="p-1.5 rounded hover:bg-blue-50 text-blue-600"><Edit size={14} /></button>
                <button onClick={() => confirm('Delete group?') && deleteMutation.mutate(group.id)} className="p-1.5 rounded hover:bg-red-50 text-red-500"><Trash2 size={14} /></button>
              </div>
            </div>
            <p className="text-sm text-gray-500 mb-3">{group.description || 'No description'}</p>
            <div className="flex items-center gap-1 text-sm text-gray-500 mb-3"><Users size={12} /><span>{group.userCount} users</span></div>
            <div className="flex flex-wrap gap-1">
              {group.roles?.map((r: any) => <span key={r.id} className="badge badge-blue">{r.name}</span>)}
              {(!group.roles || group.roles.length===0) && <span className="text-xs text-gray-400">No roles</span>}
            </div>
          </div>
        ))}
      </div>
      {modal?.type==='group' && <GroupModal group={modal.data} roles={roles} onClose={() => setModal(null)} />}
    </div>
  )
}