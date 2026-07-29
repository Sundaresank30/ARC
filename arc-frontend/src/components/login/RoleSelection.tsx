import React, { useState } from 'react';
import { Briefcase, Settings, Check, Loader2, AlertCircle } from 'lucide-react';
import { UserRole } from '../../types';
import { ManagerIllustration, OperatorIllustration } from './RoleIllustrations';
import { useAuthStore } from '../../store/authStore';

export const RoleSelection: React.FC = () => {
  const [selectedRole, setSelectedRole] = useState<UserRole>('operator');
  const { login, isLoading, error, clearError } = useAuthStore();

  const handleContinue = async () => {
    clearError();

    try {
      await login(selectedRole);
    } catch {
      // Error is stored in auth state and displayed below.
    }
  };

  return (
    <div className="w-full max-w-[780px] bg-white rounded-3xl p-8 sm:p-12 shadow-2xl shadow-gray-200/80 border border-gray-100 animate-fade-in relative overflow-hidden">
      <div className="mb-8">
        <h1 className="text-3xl sm:text-3xl font-bold text-gray-900 tracking-tight">
          Choose Your Role
        </h1>
        <p className="mt-2 text-base text-gray-500 font-normal">
          Choose your role to access the appropriate dashboard and tools.
        </p>
      </div>

      {error && (
        <div className="mb-6 flex items-center space-x-2 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-10">
        <div
          onClick={() => !isLoading && setSelectedRole('manager')}
          className={`role-card cursor-pointer rounded-2xl border-2 p-6 flex flex-col items-center text-center relative transition-all duration-200 ${
            selectedRole === 'manager'
              ? 'border-[#5E40FF] bg-indigo-50/20 shadow-lg shadow-indigo-500/10 ring-2 ring-[#5E40FF]/20'
              : 'border-gray-200 hover:border-gray-300 bg-white hover:bg-gray-50/50'
          } ${isLoading ? 'pointer-events-none opacity-70' : ''}`}
        >
          {selectedRole === 'manager' && (
            <div className="absolute top-4 right-4 bg-[#5E40FF] text-white p-1.5 rounded-full shadow-md animate-fade-in">
              <Check className="w-4 h-4 stroke-[3]" />
            </div>
          )}

          <div className="h-44 flex items-center justify-center mb-4 w-full">
            <ManagerIllustration />
          </div>

          <div className="w-12 h-12 rounded-xl bg-indigo-50 flex items-center justify-center text-[#5E40FF] mb-4 shadow-sm">
            <Briefcase className="w-6 h-6 stroke-[2]" />
          </div>

          <h2 className="text-xl font-bold text-gray-900 mb-2">Manager</h2>
          <p className="text-sm text-gray-500 leading-relaxed max-w-[220px]">
            Upload data, manage production, and review reports
          </p>
        </div>

        <div
          onClick={() => !isLoading && setSelectedRole('operator')}
          className={`role-card cursor-pointer rounded-2xl border-2 p-6 flex flex-col items-center text-center relative transition-all duration-200 ${
            selectedRole === 'operator'
              ? 'border-[#5E40FF] bg-indigo-50/20 shadow-lg shadow-indigo-500/10 ring-2 ring-[#5E40FF]/20'
              : 'border-gray-200 hover:border-gray-300 bg-white hover:bg-gray-50/50'
          } ${isLoading ? 'pointer-events-none opacity-70' : ''}`}
        >
          {selectedRole === 'operator' && (
            <div className="absolute top-4 right-4 bg-[#5E40FF] text-white p-1.5 rounded-full shadow-md animate-fade-in">
              <Check className="w-4 h-4 stroke-[3]" />
            </div>
          )}

          <div className="h-44 flex items-center justify-center mb-4 w-full">
            <OperatorIllustration />
          </div>

          <div className="w-12 h-12 rounded-xl bg-indigo-50 flex items-center justify-center text-[#5E40FF] mb-4 shadow-sm">
            <Settings className="w-6 h-6 stroke-[2]" />
          </div>

          <h2 className="text-xl font-bold text-gray-900 mb-2">Operator</h2>
          <p className="text-sm text-gray-500 leading-relaxed max-w-[220px]">
            Monitor production, track progress, and oversee operations
          </p>
        </div>
      </div>

      <div className="flex items-center justify-end space-x-4 pt-2">
        <button
          type="button"
          disabled={isLoading}
          className="px-8 py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold text-base transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-gray-300 disabled:opacity-50"
        >
          Back
        </button>

        <button
          type="button"
          onClick={handleContinue}
          disabled={!selectedRole || isLoading}
          className="px-9 py-3 rounded-xl bg-[#5E40FF] hover:bg-[#4b2bee] active:bg-[#3d1edd] text-white font-semibold text-base transition-all duration-150 shadow-lg shadow-indigo-500/25 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-indigo-400 flex items-center space-x-2"
        >
          {isLoading && <Loader2 className="w-4 h-4 animate-spin" />}
          <span>{isLoading ? 'Signing in...' : 'Continue'}</span>
        </button>
      </div>
    </div>
  );
};

export default RoleSelection;
