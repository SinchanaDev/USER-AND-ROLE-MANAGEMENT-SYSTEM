import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { authApi } from '../services/api'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'
import { ShieldCheck } from 'lucide-react'

const QUESTIONS = [
  "What is the name of your first pet?",
  "What city were you born in?",
  "What is your mother's maiden name?",
  "What was the name of your first school?",
  "What is your favorite book?",
]

export default function CompleteProfilePage() {
  const navigate = useNavigate()
  const { updateUser } = useAuthStore()
  const { register, handleSubmit } = useForm()

  const onSubmit = async (data: any) => {
    if (data.newPassword !== data.confirmPassword) { toast.error('Passwords do not match'); return }
    try {
      await authApi.completeProfile(data)
      updateUser({ firstLogin: false, firstName: data.firstName, lastName: data.lastName })
      toast.success('Profile completed!')
      navigate('/dashboard')
    } catch (err: any) { toast.error(err.response?.data?.message || 'Failed') }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-900 to-indigo-900 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg p-8">
        <div className="text-center mb-6">
          <div className="w-12 h-12 bg-blue-600 rounded-xl flex items-center justify-center mx-auto mb-3"><ShieldCheck size={24} className="text-white" /></div>
          <h1 className="text-xl font-bold">Complete Your Profile</h1>
          <p className="text-gray-500 text-sm mt-1">Fill in your details to continue</p>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div><label className="label">First Name *</label><input {...register('firstName',{required:true})} className="input" /></div>
            <div><label className="label">Last Name *</label><input {...register('lastName',{required:true})} className="input" /></div>
          </div>
          <div><label className="label">Phone</label><input {...register('phoneNumber')} className="input" /></div>
          <div>
            <label className="label">Secret Question *</label>
            <select {...register('secretQuestion',{required:true})} className="input">
              <option value="">Select a question</option>
              {QUESTIONS.map(q => <option key={q} value={q}>{q}</option>)}
            </select>
          </div>
          <div><label className="label">Secret Answer *</label><input {...register('secretAnswer',{required:true})} className="input" /></div>
          <div><label className="label">New Password *</label><input {...register('newPassword',{required:true,minLength:8})} type="password" className="input" placeholder="Min 8 chars, uppercase, digit, special char" /></div>
          <div><label className="label">Confirm Password *</label><input {...register('confirmPassword',{required:true})} type="password" className="input" /></div>
          <button type="submit" className="btn-primary w-full">Complete Profile</button>
        </form>
      </div>
    </div>
  )
}