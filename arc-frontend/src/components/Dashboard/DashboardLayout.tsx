import React, { useState, useEffect } from 'react';
import { Calendar } from 'lucide-react';
import { Sidebar } from './Sidebar';
import { StatusCards } from './StatusCards';
import { ProductionExceptions } from './ProductionExceptions';
import { LeakageTestingView } from '../LeakageTesting/LeakageTestingView';
import { DataEmbossingPage } from '../DataEmbossing/DataEmbossingPage';
import { MachinePage } from '../Machine/MachinePage';
import { useAuthStore } from '../../store/authStore';
import { getDefaultTab, modulesToTabs } from '../../utils/navigation';
import { DataPreparationPage } from '../DataPreparation/DataPreparationPage';

interface DashboardLayoutProps {
  onSignOut: () => void;
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({ onSignOut }) => {
  const { role, modules } = useAuthStore();
  const allowedTabs = modulesToTabs(modules);
  const defaultTab = role ? getDefaultTab(role) : 'dashboard';
  const [currentTab, setCurrentTab] = useState(defaultTab);

  useEffect(() => {
    if (role) {
      setCurrentTab(getDefaultTab(role));
    }
  }, [role]);

  const activeTab = allowedTabs.includes(currentTab) ? currentTab : defaultTab;

  // Backend state for Dashboard KPIs & exceptions
  const [dashboardData, setDashboardData] = useState<{
    completedCount: number;
    failedCount: number;
    totalBatches: number;
    carryForwardEmbossing: any[];
    leakageTestingFailures: any[];
  }>({
    completedCount: 460,
    failedCount: 3,
    totalBatches: 5,
    carryForwardEmbossing: [
      { id: '1', partNo: 'Pn00111c', serialNo: 'P0011156', status: 'Pending', remainingSince: '17:57, 20 Jul', nextShift: '21 Jul', action: 'Queued' },
      { id: '2', partNo: 'Pn00112c', serialNo: 'P0011157', status: 'Pending', remainingSince: '17:58, 20 Jul', nextShift: '21 Jul', action: 'Queued' },
      { id: '3', partNo: 'Pn00113c', serialNo: 'P0011158', status: 'Pending', remainingSince: '18:00, 20 Jul', nextShift: '21 Jul', action: 'Queued' },
    ],
    leakageTestingFailures: [
      { id: '1', partNo: 'Pn00111c', serialNo: 'P0011156', status: 'Failed', testValue: 0.42, direction: 'down', timestamp: '17:57, 20 Jul', attempt: '2/2', action: 'Scrap' },
      { id: '2', partNo: 'Pn00112c', serialNo: 'P0011157', status: 'Failed', testValue: 1.08, direction: 'up', timestamp: '17:58, 20 Jul', attempt: '1/2', action: 'Pending' },
      { id: '3', partNo: 'Pn00113c', serialNo: 'P0011158', status: 'Failed', testValue: 0.48, direction: 'down', timestamp: '18:00, 20 Jul', attempt: '1/2', action: 'Pending' },
    ],
  });

  const fetchDashboardData = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/dashboard');
      if (res.ok) {
        const data = await res.json();
        setDashboardData(data);
      }
    } catch (err) {
      console.warn('Backend API offline, using local state.');
    }
  };

  useEffect(() => {
    if (role === 'manager') {
      fetchDashboardData();
    }
  }, [role]);

  const handleResolveCarryForward = async (id: string, partNo: string) => {
    try {
      await fetch(`http://localhost:8080/api/dashboard/carry-forward/${id}/resolve`, { method: 'POST' });
    } catch (e) {}

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
      await fetch(`http://localhost:8080/api/dashboard/leakage-failures/${id}/resolve`, { method: 'POST' });
    } catch (e) {}

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
    return new Date().toLocaleDateString(undefined, {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  };

  if (!role) {
    return null;
  }

  return (
    <div className="min-h-screen bg-[#F4F5F8] flex w-full">
      <Sidebar
        currentTab={activeTab}
        setCurrentTab={setCurrentTab}
        allowedTabs={allowedTabs}
        selectedRole={role}
        onSignOut={onSignOut}
      />

      <main className="flex-1 p-6 sm:p-10 max-w-7xl mx-auto overflow-y-auto">
        {activeTab === 'dashboard' ? (
          <div className="animate-fade-in space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <h1 className="text-[28px] font-bold text-gray-900 tracking-tight leading-tight">
                  Welcome Back
                </h1>
                <p className="mt-1 text-sm sm:text-base text-gray-500 font-medium">
                  Monitor production, track batches, and review automated process updates
                </p>
              </div>

              <div className="flex items-center space-x-2 bg-white border border-gray-150 px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
                <Calendar className="w-4 h-4 text-gray-500" />
                <span className="text-sm font-semibold text-gray-700">{getFormattedDate()}</span>
              </div>
            </div>

            <div>
              <h2 className="text-lg font-bold text-gray-800 tracking-tight mb-4">
                Production Status
              </h2>
              <StatusCards
                completedCount={dashboardData.completedCount}
                failedCount={dashboardData.failedCount}
                totalBatches={dashboardData.totalBatches}
              />
            </div>

            {/* Production Exceptions Table Section */}
            <ProductionExceptions
              carryForwardData={dashboardData.carryForwardEmbossing}
              leakageFailuresData={dashboardData.leakageTestingFailures}
              onResolveCarryForward={handleResolveCarryForward}
              onResolveLeakage={handleResolveLeakage}
            />
          </div>
        ) : activeTab === 'leakage-testing' ? (
          <LeakageTestingView />
        ) : activeTab === 'data-embossing' ? (
          <DataEmbossingPage />
        ) : activeTab === 'machine' ? (
          <MachinePage />
        ) : activeTab === 'data-preparation' ? (
          <DataPreparationPage />
        ) : (
          <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-150 animate-fade-in">
            <h1 className="text-2xl font-bold text-gray-900 capitalize mb-4">
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

