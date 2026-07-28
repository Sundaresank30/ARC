import React, { useState } from 'react';
import { Briefcase, Settings, Check } from 'lucide-react';
import { UserRole } from '../types';
import { ManagerIllustration, OperatorIllustration } from './RoleIllustrations';

interface RoleSelectionProps {
  onSelectRole: (role: UserRole) => void;
  onBack?: () => void;
}

export const RoleSelection: React.FC<RoleSelectionProps> = ({ onSelectRole, onBack }) => {
  const [selectedRole, setSelectedRole] = useState<UserRole>('operator'); // Default selected as shown or selectable

  const handleContinue = () => {
    if (selectedRole) {
      onSelectRole(selectedRole);
    }
  };

  return (
    <div className="w-full max-w-[780px] bg-white rounded-3xl p-8 sm:p-12 shadow-2xl shadow-gray-200/80 border border-gray-100 animate-fade-in relative overflow-hidden">
      {/* Top Header */}
      <div className="mb-8">
        <h1 className="text-3xl sm:text-3xl font-bold text-gray-900 tracking-tight">
          Choose Your Role
        </h1>
        <p className="mt-2 text-base text-gray-500 font-normal">
          Choose your role to access the appropriate dashboard and tools.
        </p>
      </div>

      {/* Role Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-10">
        
        {/* Manager Card */}
        <div
          onClick={() => setSelectedRole('manager')}
          className={`role-card cursor-pointer rounded-2xl border-2 p-6 flex flex-col items-center text-center relative transition-all duration-200 ${
            selectedRole === 'manager'
              ? 'border-[#5E40FF] bg-indigo-50/20 shadow-lg shadow-indigo-500/10 ring-2 ring-[#5E40FF]/20'
              : 'border-gray-200 hover:border-gray-300 bg-white hover:bg-gray-50/50'
          }`}
        >
          {/* Active Check Indicator */}
          {selectedRole === 'manager' && (
            <div className="absolute top-4 right-4 bg-[#5E40FF] text-white p-1.5 rounded-full shadow-md animate-fade-in">
              <Check className="w-4 h-4 stroke-[3]" />
            </div>
          )}

          {/* Top Graphic Illustration */}
          <div className="h-44 flex items-center justify-center mb-4 w-full">
            <ManagerIllustration />
          </div>

          {/* Badge Icon */}
          <div className="w-12 h-12 rounded-xl bg-indigo-50 flex items-center justify-center text-[#5E40FF] mb-4 shadow-sm">
            <Briefcase className="w-6 h-6 stroke-[2]" />
          </div>

          {/* Title */}
          <h2 className="text-xl font-bold text-gray-900 mb-2">
            Manager
          </h2>

          {/* Subtitle / Description */}
          <p className="text-sm text-gray-500 leading-relaxed max-w-[220px]">
            Upload data, manage production, and review reports
          </p>
        </div>

        {/* Operator Card */}
        <div
          onClick={() => setSelectedRole('operator')}
          className={`role-card cursor-pointer rounded-2xl border-2 p-6 flex flex-col items-center text-center relative transition-all duration-200 ${
            selectedRole === 'operator'
              ? 'border-[#5E40FF] bg-indigo-50/20 shadow-lg shadow-indigo-500/10 ring-2 ring-[#5E40FF]/20'
              : 'border-gray-200 hover:border-gray-300 bg-white hover:bg-gray-50/50'
          }`}
        >
          {/* Active Check Indicator */}
          {selectedRole === 'operator' && (
            <div className="absolute top-4 right-4 bg-[#5E40FF] text-white p-1.5 rounded-full shadow-md animate-fade-in">
              <Check className="w-4 h-4 stroke-[3]" />
            </div>
          )}

          {/* Top Graphic Illustration */}
          <div className="h-44 flex items-center justify-center mb-4 w-full">
            <OperatorIllustration />
          </div>

          {/* Badge Icon */}
          <div className="w-12 h-12 rounded-xl bg-indigo-50 flex items-center justify-center text-[#5E40FF] mb-4 shadow-sm">
            <Settings className="w-6 h-6 stroke-[2]" />
          </div>

          {/* Title */}
          <h2 className="text-xl font-bold text-gray-900 mb-2">
            Operator
          </h2>

          {/* Subtitle / Description */}
          <p className="text-sm text-gray-500 leading-relaxed max-w-[220px]">
            Monitor production, Track progress, and oversee operations
          </p>
        </div>

      </div>

      {/* Action Buttons Footer */}
      <div className="flex items-center justify-end space-x-4 pt-2">
        <button
          type="button"
          onClick={() => onBack ? onBack() : alert('Already at initial setup step')}
          className="px-8 py-3 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold text-base transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-gray-300"
        >
          Back
        </button>

        <button
          type="button"
          onClick={handleContinue}
          disabled={!selectedRole}
          className="px-9 py-3 rounded-xl bg-[#5E40FF] hover:bg-[#4b2bee] active:bg-[#3d1edd] text-white font-semibold text-base transition-all duration-150 shadow-lg shadow-indigo-500/25 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-indigo-400"
        >
          Continue
        </button>
      </div>
    </div>
  );
};
