import React, { useState } from 'react';
import { UserRole } from './types';
import { RoleSelection } from './components/RoleSelection';
import { Factory } from 'lucide-react';

export function App() {
  const [selectedRole, setSelectedRole] = useState<UserRole>('operator');

  const handleSelectRole = (role: UserRole) => {
    setSelectedRole(role);
    console.log('Selected Role:', role);
  };

  return (
    <div className="min-h-screen bg-[#F4F5F8] flex flex-col items-center justify-center p-4 sm:p-6 select-none relative overflow-hidden">
      
      {/* Background ambient decorative shapes */}
      <div className="absolute -top-40 -left-40 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-40 -right-40 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />

      {/* Top Branding Header */}
      <div className="mb-6 flex items-center space-x-3 text-gray-800">
        <div className="w-10 h-10 rounded-xl bg-[#5E40FF] text-white flex items-center justify-center shadow-lg shadow-indigo-500/30">
          <Factory className="w-6 h-6" />
        </div>
        <div>
          <span className="text-xl font-bold tracking-tight text-gray-900 block leading-none">
            ARC Production Suite
          </span>
          <span className="text-xs font-medium text-gray-500">
            Industrial Line Operating System
          </span>
        </div>
      </div>

      {/* Dedicated Standalone "Choose Your Role" Screen */}
      <div className="w-full flex items-center justify-center z-10">
        <RoleSelection
          onSelectRole={handleSelectRole}
          onBack={() => {
            console.log('Back clicked');
          }}
        />
      </div>

      {/* Footer copyright */}
      <footer className="mt-8 text-xs text-gray-400 font-medium z-10">
        ARC Enterprise Production System &bull; Role Selection Portal
      </footer>
    </div>
  );
}

export default App;
