import React, { useState } from 'react';
import { Calendar } from 'lucide-react';
import { Sidebar } from './Sidebar';
import { StatusCards } from './StatusCards';
import { ProductionExceptions } from './ProductionExceptions';
import { LeakageTestingView } from '../LeakageTesting/LeakageTestingView';
import { DataEmbossingPage } from '../DataEmbossing/DataEmbossingPage';
import { MachinePage } from '../Machine/MachinePage';
import { UserRole } from '../../types';

interface DashboardLayoutProps {
  selectedRole: UserRole;
  onSignOut: () => void;
}

export const DashboardLayout: React.FC<DashboardLayoutProps> = ({
  selectedRole,
  onSignOut,
}) => {
  // Allowed tabs per role definition
  const allowedTabs = selectedRole === 'operator'
    ? ['data-embossing', 'leakage-testing', 'settings']
    : ['dashboard', 'data-preparation', 'machine', 'settings'];

  const defaultTab = selectedRole === 'operator' ? 'data-embossing' : 'dashboard';
  const [currentTab, setCurrentTab] = useState(defaultTab);

  // Sync default tab if selectedRole changes
  React.useEffect(() => {
    setCurrentTab(selectedRole === 'operator' ? 'data-embossing' : 'dashboard');
  }, [selectedRole]);

  // Ensure user cannot stay on an unauthorized tab
  const activeTab = allowedTabs.includes(currentTab) ? currentTab : defaultTab;

  // Hardcode or get formatted date "20 July, 2026" or current date
  const getFormattedDate = () => {
    return '20 July, 2026';
  };

  return (
    <div className="min-h-screen bg-[#F4F5F8] flex w-full">
      {/* Sidebar Panel */}
      <Sidebar
        currentTab={activeTab}
        setCurrentTab={setCurrentTab}
        selectedRole={selectedRole}
        onSignOut={onSignOut}
      />

      {/* Main Container Area */}
      <main className="flex-1 p-6 sm:p-10 max-w-7xl mx-auto overflow-y-auto">
        {activeTab === 'dashboard' ? (
          <div className="animate-fade-in space-y-6">

            {/* Top Workspace Header */}
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
              <div>
                <h1 className="text-[28px] font-bold text-gray-900 tracking-tight leading-tight">
                  Welcome Back
                </h1>
                <p className="mt-1 text-sm sm:text-base text-gray-500 font-medium">
                  Monitor production, track batches, and review automated process updates
                </p>
              </div>

              {/* Date Badge */}
              <div className="flex items-center space-x-2 bg-white border border-gray-150 px-4 py-2 rounded-xl shadow-sm self-start sm:self-auto">
                <Calendar className="w-4 h-4 text-gray-500" />
                <span className="text-sm font-semibold text-gray-700">
                  {getFormattedDate()}
                </span>
              </div>
            </div>

            {/* Production Status KPI Section */}
            <div>
              <h2 className="text-lg font-bold text-gray-800 tracking-tight mb-4">
                Production Status
              </h2>
              <StatusCards
                completedCount={498}
                failedCount={3}
                totalBatches={5}
              />
            </div>

            {/* Production Exceptions Table Section */}
            <ProductionExceptions />

          </div>
        ) : activeTab === 'leakage-testing' ? (
          <LeakageTestingView />
        ) : activeTab === 'machine' ? (
          <MachinePage />
        ) : activeTab === 'data-embossing' ? (
          <DataEmbossingPage />
        ) : (
          <div className="bg-white rounded-3xl p-8 shadow-sm border border-gray-150 animate-fade-in">
            <h1 className="text-2xl font-bold text-gray-900 capitalize mb-4">
              {activeTab.replace('-', ' ')}
            </h1>
            <p className="text-gray-500">
              This section is currently under development or disabled for the demo.
            </p>
          </div>
        )}
      </main>
    </div>
  );
};
export default DashboardLayout;


