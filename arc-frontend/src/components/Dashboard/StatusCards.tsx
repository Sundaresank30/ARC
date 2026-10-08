import React from 'react';
import { CheckCircle2, AlertCircle, FileText } from 'lucide-react';

interface StatusCardProps {
  completedCount: number;
  failedCount: number;
  totalBatches: number;
}

export const StatusCards: React.FC<StatusCardProps> = ({
  completedCount = 0,
  failedCount = 0,
  totalBatches = 0,
}) => {
  const cards = [
    {
      title: 'Completed',
      value: completedCount,
      changeText: '+from this week',
      icon: CheckCircle2,
      subtextColorClass: 'text-[#16a34a]',
      iconBgClass: 'bg-[#0e2a18] border border-[#16a34a]/30 text-[#22c55e]',
    },
    {
      title: 'Failed',
      value: failedCount,
      changeText: '-from this week',
      icon: AlertCircle,
      subtextColorClass: 'text-[#dc2626]',
      iconBgClass: 'bg-[#2e0e11] border border-[#dc2626]/30 text-[#ef4444]',
    },
    {
      title: 'Total batches',
      value: totalBatches,
      changeText: '+from this week',
      icon: FileText,
      subtextColorClass: 'text-[#00d8f6]',
      iconBgClass: 'bg-[#19272e] border border-[#00d8f6]/30 text-[#00d8f6]',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 w-full mb-8">
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div
            key={idx}
            className="bg-[#121417] border border-[#1e232a] rounded-2xl p-6 shadow-sm flex flex-col justify-between h-full hover:border-[#00d8f6]/50 hover:shadow-[0_0_20px_rgba(0,216,246,0.15)] transition-all duration-200"
          >
            {/* Top row: Title (left) & Icon (right) */}
            <div className="flex items-center justify-between">
              <span className="font-semibold text-[15px] text-white">
                {card.title}
              </span>
              <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${card.iconBgClass}`}>
                <Icon className="w-4.5 h-4.5" />
              </div>
            </div>

            {/* Middle: Large Value */}
            <div className="my-2">
              <span className="text-[44px] font-bold leading-none tracking-tight text-white">
                {card.value}
              </span>
            </div>

            {/* Bottom: Subtext */}
            <div>
              <span className={`text-xs font-semibold ${card.subtextColorClass}`}>
                {card.changeText}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
};
export default StatusCards;
