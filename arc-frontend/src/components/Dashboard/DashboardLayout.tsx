import React, { useState, useEffect } from 'react';
import { Calendar } from 'lucide-react';
import { Sidebar } from './Sidebar';
import { StatusCards } from './StatusCards';
import { ProductionExceptions } from './ProductionExceptions';
import { LeakageTestingView } from '../LeakageTesting/LeakageTestingView';
import { LeakageMachinePage } from '../LeakageTesting/LeakageMachinePage';
import { DataEmbossingPage } from '../DataEmbossing/DataEmbossingPage';
import { MachinePage } from '../Machine/MachinePage';
import { useAuthStore } from '../../store/authStore';
import { getDefaultTab, modulesToTabs } from '../../utils/navigation';
import { DataPreparationPage } from '../DataPreparation/DataPreparationPage';
import { UserRole } from '../../types';
import {
  getDashboardSummary,
  resolveCarryForward,
  resolveLeakageFailure,
} from '../../api/dashboard';

interface DashboardLayoutProps {
  selectedRole?: UserRole;
  onSignOut: () => void;
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({ selectedRole: propRole, onSignOut }) => {
  const { role: storeRole, modules } = useAuthStore();
  const effectiveRole = storeRole || propRole || 'manager';

  const rawTabs = modules.length > 0
    ? modulesToTabs(modules)
    : effectiveRole === 'operator'
      ? ['data-embossing', 'leakage-machine', 'leakage-testing', 'machine', 'settings']
      : ['dashboard', 'data-preparation', 'settings'];

  // Always inject leakage-machine for operators right after data-embossing,
  // even if the cached session modules don't include it yet.
  const allowedTabs = (() => {
    if (effectiveRole !== 'operator' || rawTabs.includes('leakage-machine')) {
      return rawTabs;
    }
    const idx = rawTabs.indexOf('data-embossing');
    if (idx !== -1) {
      const updated = [...rawTabs];
      updated.splice(idx + 1, 0, 'leakage-machine');
      return updated;
    }
    return [...rawTabs, 'leakage-machine'];
  })();

  const defaultTab = getDefaultTab ? getDefaultTab(effectiveRole) : (effectiveRole === 'operator' ? 'machine' : 'dashboard');
  const [currentTab, setCurrentTab] = useState(defaultTab);

  useEffect(() => {
    setCurrentTab(defaultTab);
  }, [effectiveRole]);

  // Backend state for Dashboard KPIs & exceptions
  const [dashboardData, setDashboardData] = useState<{
    completedCount: number;
    failedCount: number;
    totalBatches: number;
    carryForwardEmbossing: any[];
    leakageTestingFailures: any[];
  }>({
    completedCount: 0,
    failedCount: 0,
    totalBatches: 0,
    carryForwardEmbossing: [],
    leakageTestingFailures: [],
  });

  const fetchDashboardData = async () => {
    try {
      const data = await getDashboardSummary();
      setDashboardData(data);
    } catch (err) {
      console.warn('Backend API offline, using local state.');
    }
  };

  useEffect(() => {
    if (effectiveRole.toLowerCase() === 'manager') {
      fetchDashboardData();
    }
  }, [effectiveRole]);

  const activeTab = allowedTabs.includes(currentTab) ? currentTab : defaultTab;

  const handleResolveCarryForward = async (id: string, partNo: string) => {
    try {
      await resolveCarryForward(id);
    } catch (e) { }

    setDashboardData((prev) => {
      const newCarryForward = prev.carryForwardEmbossing.filter((item) => item.id !== id);
      const newLeakage = prev.leakageTestingFailures;
      const isAllClear = newCarryForward.length === 0 && newLeakage.length === 0;
      return {
        ...prev,
        carryForwardEmbossing: newCarryForward,
        completedCount: isAllClear ? 498 : prev.completedCount,
      };
    });
  };

  const handleResolveLeakage = async (id: string, partNo: string) => {
    try {
      await resolveLeakageFailure(id);
    } catch (e) { }

    setDashboardData((prev) => {
      const newLeakage = prev.leakageTestingFailures.filter((item) => item.id !== id);
      const newCarryForward = prev.carryForwardEmbossing;
      const isAllClear = newCarryForward.length === 0 && newLeakage.length === 0;
      return {
        ...prev,
        leakageTestingFailures: newLeakage,
        failedCount: newLeakage.length,
        completedCount: isAllClear ? 498 : prev.completedCount,
      };
    });
  };

  const getFormattedDate = () => {
    return new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' });
  };

  return (
    <div className="min-h-screen bg-[#06020c] flex w-full">
      <Sidebar
        currentTab={activeTab}
        setCurrentTab={setCurrentTab}
        allowedTabs={allowedTabs}
        selectedRole={effectiveRole as UserRole}
        onSignOut={onSignOut}
      />

      <main className="flex-1 p-6 sm:p-10 max-w-7xl mx-auto overflow-y-auto">
        {activeTab === 'dashboard' ? (
          <div className="animate-fade-in space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <h1 className="text-[28px] font-bold text-white tracking-tight leading-tight">
                  Welcome Back
                </h1>
                <p className="mt-1 text-sm sm:text-base text-[#8a8596] font-medium">
                  Monitor production, track batches, and review automated process updates
                </p>
              </div>

              <div className="flex items-center space-x-2 bg-[#13111c] border border-[#221e33] px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
                <Calendar className="w-4 h-4 text-gray-400" />
                <span className="text-sm font-semibold text-gray-300">{getFormattedDate()}</span>
              </div>
            </div>

            <div>
              <h2 className="text-lg font-bold text-white tracking-tight mb-4">
                Production Status
              </h2>
              <StatusCards
                completedCount={dashboardData.completedCount}
                failedCount={dashboardData.failedCount}
                totalBatches={dashboardData.totalBatches}
              />
            </div>

            <ProductionExceptions
              carryForwardData={dashboardData.carryForwardEmbossing}
              leakageFailuresData={dashboardData.leakageTestingFailures}
              onResolveCarryForward={handleResolveCarryForward}
              onResolveLeakage={handleResolveLeakage}
            />
          </div>
        ) : activeTab === 'data-embossing' ? (
          <DataEmbossingPage />
        ) : activeTab === 'leakage-machine' ? (
          <LeakageMachinePage />
        ) : activeTab === 'leakage-testing' ? (
          <LeakageTestingView />
        ) : activeTab === 'machine' ? (
          <MachinePage />
        ) : activeTab === 'data-preparation' ? (
          <DataPreparationPage />
        ) : (
          <div className="bg-[#0D0E19] rounded-3xl p-8 shadow-sm border border-[#1b172a] animate-fade-in">
            <h1 className="text-2xl font-bold text-white capitalize mb-4">
              {activeTab.replace('-', ' ')}
            </h1>
            <p className="text-gray-500">
              This section is currently under development.
            </p>
          </div>
        )}
      </main>
    </div>
  );
};

export default DashboardLayout;
