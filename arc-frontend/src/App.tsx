import React, { useEffect } from 'react';
import { Factory, Loader2 } from 'lucide-react';
import { RoleSelection } from './components/login/RoleSelection';
import { DashboardLayout } from './components/Dashboard/DashboardLayout';
import { setUnauthorizedHandler } from './api/client';
import { useAuthStore } from './store/authStore';

export function App() {
  const {
    isAuthenticated,
    isLoading,
    role,
    logout,
    restoreSession,
  } = useAuthStore();

  useEffect(() => {
    if (useAuthStore.persist.hasHydrated()) {
      restoreSession();
      return;
    }

    return useAuthStore.persist.onFinishHydration(() => {
      restoreSession();
    });
  }, [restoreSession]);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      logout();
    });
  }, [logout]);

  if (isLoading && !isAuthenticated) {
    return (
      <div className="min-h-screen bg-[#F4F5F8] flex items-center justify-center">
        <div className="flex flex-col items-center space-y-3 text-gray-600">
          <Loader2 className="w-8 h-8 animate-spin text-[#5E40FF]" />
          <span className="text-sm font-semibold">Restoring session...</span>
        </div>
      </div>
    );
  }

  if (isAuthenticated && role) {
    return <DashboardLayout onSignOut={logout} />;
  }

  return (
    <div className="min-h-screen bg-[#F4F5F8] flex flex-col items-center justify-center p-4 sm:p-6 select-none relative overflow-hidden">
      <div className="absolute -top-40 -left-40 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute -bottom-40 -right-40 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />

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

      <div className="w-full flex items-center justify-center z-10">
        <RoleSelection />
      </div>

      <footer className="mt-8 text-xs text-gray-400 font-medium z-10">
        ARC Enterprise Production System &bull; Role Selection Portal
      </footer>
    </div>
  );
}

export default App;
