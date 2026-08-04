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
    <div className="w-full max-w-[780px] bg-[#0d0b14] rounded-3xl p-8 sm:p-12 shadow-[0_0_40px_rgba(139,92,246,0.15)] border border-[#1b172a] animate-fade-in relative overflow-hidden">
      <div className="mb-8">
        <h1 className="text-3xl sm:text-3xl font-bold text-white tracking-tight">
          Choose Your Role
        </h1>
        <p className="mt-2 text-base text-gray-400 font-normal">
          Choose your role to access the appropriate dashboard and tools.
        </p>
      </div>

      {error && (
        <div className="mb-6 flex items-center space-x-2 rounded-xl border border-red-900 bg-red-950/40 px-4 py-3 text-sm font-semibold text-red-400">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{error}</span>
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-10">
        <div
          onClick={() => !isLoading && setSelectedRole('manager')}
          className={`role-card cursor-pointer rounded-2xl border-2 p-6 flex flex-col items-center text-center relative transition-all duration-200 ${
            selectedRole === 'manager'
              ? 'border-[#7c3aed] bg-[#120e24] shadow-lg shadow-purple-500/5 ring-1 ring-[#7c3aed]/20'
              : 'border-[#1c1827] bg-[#07050d] hover:border-[#2b243d] hover:bg-[#0c0914]'
          } ${isLoading ? 'pointer-events-none opacity-70' : ''}`}
        >
          <div className="h-44 flex items-center justify-center mb-4 w-full">
            <ManagerIllustration />
          </div>

          <div className="w-12 h-12 rounded-xl bg-[#19122a] flex items-center justify-center text-[#8b5cf6] mb-4 shadow-sm">
            <Briefcase className="w-6 h-6 stroke-[2]" />
          </div>

          <h2 className="text-xl font-bold text-white mb-2">Manager</h2>
          <p className="text-sm text-gray-400 leading-relaxed max-w-[220px]">
            Upload data, manage production, and review reports
          </p>
        </div>

        <div
          onClick={() => !isLoading && setSelectedRole('operator')}
          className={`role-card cursor-pointer rounded-2xl border-2 p-6 flex flex-col items-center text-center relative transition-all duration-200 ${
            selectedRole === 'operator'
              ? 'border-[#7c3aed] bg-[#120e24] shadow-lg shadow-purple-500/5 ring-1 ring-[#7c3aed]/20'
              : 'border-[#1c1827] bg-[#07050d] hover:border-[#2b243d] hover:bg-[#0c0914]'
          } ${isLoading ? 'pointer-events-none opacity-70' : ''}`}
        >
          <div className="h-44 flex items-center justify-center mb-4 w-full">
            <OperatorIllustration />
          </div>

          <div className="w-12 h-12 rounded-xl bg-[#19122a] flex items-center justify-center text-[#8b5cf6] mb-4 shadow-sm">
            <Settings className="w-6 h-6 stroke-[2]" />
          </div>

          <h2 className="text-xl font-bold text-white mb-2">Operator</h2>
          <p className="text-sm text-gray-400 leading-relaxed max-w-[220px]">
            Monitor production, Track progress, and oversee operations
          </p>
        </div>
      </div>

      <div className="flex items-center justify-end space-x-4 pt-2">
        <button
          type="button"
          disabled={isLoading}
          className="px-8 py-3 rounded-xl bg-[#13111c] hover:bg-[#1a1726] border border-[#221e33] text-gray-300 font-semibold text-base transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-purple-900 disabled:opacity-50"
        >
          Back
        </button>

        <button
          type="button"
          onClick={handleContinue}
          disabled={!selectedRole || isLoading}
          className="px-9 py-3 rounded-xl bg-[#7c3aed] hover:bg-[#6d28d9] active:bg-[#5b21b6] text-white font-semibold text-base transition-all duration-150 shadow-lg shadow-purple-500/20 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-purple-400 flex items-center space-x-2"
        >
          {isLoading && <Loader2 className="w-4 h-4 animate-spin" />}
          <span>{isLoading ? 'Signing in...' : 'Continue'}</span>
        </button>
      </div>
    </div>
  );
};

export default RoleSelection;
