import React from 'react';
import { 
  LayoutGrid, 
  LayoutDashboard, 
  Database, 
  Hammer, 
  Droplet, 
  Settings, 
  Lock, 
  LogOut 
} from 'lucide-react';
import { UserRole } from '../../types';

interface SidebarProps {
  currentTab: string;
  setCurrentTab: (tab: string) => void;
  selectedRole: UserRole;
  onSignOut: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ 
  currentTab, 
  setCurrentTab, 
  selectedRole, 
  onSignOut 
}) => {
  const menuItems = [
    {
      id: 'dashboard',
      label: 'Dashboard',
      icon: LayoutDashboard,
      disabled: false,
    },
    {
      id: 'data-preparation',
      label: 'Data Preparation',
      icon: Database,
      disabled: false,
    },
    {
      id: 'data-embossing',
      label: 'Data Embossing',
      icon: Hammer,
      disabled: false,
      locked: true,
      lockColor: 'green',
    },
    {
      id: 'leakage-testing',
      label: 'Leakage Testing',
      icon: Droplet,
      disabled: true,
      locked: true,
      lockColor: 'red',
    },
    {
      id: 'settings',
      label: 'Settings',
      icon: Settings,
      disabled: false,
    },
  ];

  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col justify-between h-screen sticky top-0 shrink-0 select-none">
      <div className="flex flex-col pt-6 px-4">
        {/* Branding Logo */}
        <div className="flex items-center space-x-3 px-3 mb-8">
          <div className="w-9 h-9 rounded-lg bg-[#5E40FF] text-white flex items-center justify-center shadow-md">
            <LayoutGrid className="w-5 h-5 stroke-[2.5]" />
          </div>
          <div>
            <span className="text-xl font-bold tracking-tight text-gray-900 leading-none block">
              ARC
            </span>
          </div>
        </div>

        {/* Navigation Menu */}
        <nav className="space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;
            
            return (
              <button
                key={item.id}
                onClick={() => !item.disabled && setCurrentTab(item.id)}
                disabled={item.disabled}
                className={`w-full flex items-center justify-between px-3 py-3 rounded-xl text-sm font-semibold transition-all duration-150 ${
                  isActive
                    ? 'bg-gray-100 text-gray-900 shadow-sm'
                    : item.disabled
                    ? 'text-gray-350 cursor-not-allowed'
                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                }`}
              >
                <div className="flex items-center space-x-3">
                  <Icon className={`w-5 h-5 ${
                    isActive ? 'text-gray-950' : item.disabled ? 'text-gray-300' : 'text-gray-500'
                  }`} />
                  <span className={item.disabled ? 'text-gray-350' : 'text-gray-700'}>{item.label}</span>
                </div>
                {item.locked && (
                  <Lock className={`w-4 h-4 stroke-[2.5] ${
                    item.lockColor === 'green' ? 'text-[#00B074]' : 'text-[#FF4D4D]'
                  }`} />
                )}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Footer Sign Out */}
      <div className="p-4 border-t border-gray-100">
        <button
          onClick={onSignOut}
          className="w-full flex items-center space-x-3 px-3 py-3 rounded-xl text-sm font-semibold text-gray-600 hover:bg-red-50 hover:text-red-600 transition-all duration-150"
        >
          <LogOut className="w-5 h-5 text-gray-500 hover:text-red-600" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
