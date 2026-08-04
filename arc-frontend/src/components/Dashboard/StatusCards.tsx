import React from 'react';
import { CheckCircle2, AlertCircle, FileText } from 'lucide-react';

interface StatusCardProps {
  completedCount: number;
  failedCount: number;
  totalBatches: number;
}

export const StatusCards: React.FC<StatusCardProps> = ({
  completedCount = 498,
  failedCount = 3,
  totalBatches = 5,
}) => {
  const cards = [
    {
      title: 'Completed',
      value: completedCount,
      changeText: '+from this week',
      icon: CheckCircle2,
      subtextColorClass: 'text-[#10b981]',
      iconBgClass: 'bg-[#0c1f19] border border-[#10b981]/30 text-[#10b981]',
    },
    {
      title: 'Failed',
      value: failedCount,
      changeText: '-from this week',
      icon: AlertCircle,
      subtextColorClass: 'text-[#ef4444]',
      iconBgClass: 'bg-[#271012] border border-[#ef4444]/30 text-[#ef4444]',
    },
    {
      title: 'Total batches',
      value: totalBatches,
      changeText: '+from this week',
      icon: FileText,
      subtextColorClass: 'text-[#6366f1]',
      iconBgClass: 'bg-[#141235] border border-[#6366f1]/30 text-[#6366f1]',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 w-full mb-8">
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div
            key={idx}
            className="bg-[#0d0b14] border border-[#1c182a] rounded-2xl p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow duration-150"
          >
            {/* Top row: Title (left) & Icon (right) */}
            <div className="flex items-center justify-between mb-4">
              <span className="font-bold text-[15px] text-gray-300">
                {card.title}
              </span>
              <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${card.iconBgClass}`}>
                <Icon className="w-4.5 h-4.5" />
              </div>
            </div>

            {/* Middle: Large Value */}
            <div className="mb-3">
              <span className="text-[46px] font-bold leading-none tracking-tight text-white">
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
