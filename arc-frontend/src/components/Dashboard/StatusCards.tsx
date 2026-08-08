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
      changeText: '+from actual production data',
      icon: CheckCircle2,
      subtextColorClass: 'text-[#10b981]',
      iconBgClass: 'bg-[#0c1f19] border border-[#10b981]/30 text-[#10b981]',
    },
    {
      title: 'Failed',
      value: failedCount,
      changeText: '-from actual production data',
      icon: AlertCircle,
      subtextColorClass: 'text-[#ef4444]',
      iconBgClass: 'bg-[#271012] border border-[#ef4444]/30 text-[#ef4444]',
    },
    {
      title: 'Total batches',
      value: totalBatches,
      changeText: 'total registered batches',
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
            className="bg-gradient-to-b from-[#09040A] to-[#111827]/80 border border-[#1e1b29] rounded-2xl p-6 shadow-sm flex flex-col justify-between h-full hover:shadow-md transition-shadow duration-150"
          >
            {/* Top row: Title (left) & Icon (right) */}
            <div className="flex items-center justify-between">
              <span className="font-semibold text-[15px] text-gray-300">
                {card.title}
              </span>
              <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${card.iconBgClass}`}>
                <Icon className="w-4.5 h-4.5" />
              </div>
            </div>

            {/* Middle: Large Value */}
            <div className="my-1">
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
