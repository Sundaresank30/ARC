import React from 'react';
import {
  LayoutGrid,
  LayoutDashboard,
  Database,
  Cpu,
  Hammer,
  Droplet,
  Gauge,
  Settings,
  LogOut,
} from 'lucide-react';
import { UserRole } from '../../types';

interface SidebarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  allowedTabs: string[];
  selectedRole: UserRole;
  onSignOut: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  currentTab,
  setCurrentTab,
  allowedTabs,
  selectedRole,
  onSignOut,
}) => {
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'data-preparation', label: 'Data Preparation', icon: Database },
    { id: 'machine', label: 'Machine', icon: Cpu },
    { id: 'data-embossing', label: 'Data Embossing', icon: Hammer },
    { id: 'leakage-machine', label: 'Leakage Machine', icon: Gauge },
    { id: 'leakage-testing', label: 'Leakage Testing', icon: Droplet },
    { id: 'settings', label: 'Settings', icon: Settings },
  ];

  const visibleItems = menuItems.filter((item) => allowedTabs.includes(item.id));

  return (
    <aside className="w-64 bg-[#101010] border-r border-[#1e232a] flex flex-col justify-between h-screen sticky top-0 shrink-0 select-none">
      <div className="flex flex-col pt-6 px-4">
        <div className="flex items-center px-2 mb-8">
          <img src="/assets/logo.png" alt="ARC Logo" className="h-14 object-contain max-w-full" />
        </div>

        <nav className="space-y-1">
          {visibleItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;

            return (
              <button
                key={item.id}
                onClick={() => setCurrentTab(item.id)}
                className={`w-full flex items-center justify-between px-3 py-3 rounded-xl text-sm font-semibold transition-all duration-150 ${
                  isActive
                    ? 'bg-[#00d8f6] text-[#05181e] shadow-[0_0_20px_rgba(0,216,246,0.35)] font-bold'
                    : 'text-white hover:bg-[#181a1d] hover:text-white'
                }`}
              >
                <div className="flex items-center space-x-3">
                  <Icon
                    className={`w-5 h-5 ${
                      isActive ? 'text-[#05181e]' : 'text-white'
                    }`}
                  />
                  <span className={isActive ? 'text-[#05181e] font-bold' : 'text-white font-medium'}>{item.label}</span>
                </div>
              </button>
            );
          })}
        </nav>
      </div>

      <div className="p-4 border-t border-[#1e232a]">
        <button
          onClick={onSignOut}
          className="w-full flex items-center space-x-3 px-3 py-3 rounded-xl text-sm font-semibold text-gray-300 hover:bg-red-950/20 hover:text-red-400 transition-all duration-150"
        >
          <LogOut className="w-5 h-5 text-gray-300 hover:text-red-400" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
