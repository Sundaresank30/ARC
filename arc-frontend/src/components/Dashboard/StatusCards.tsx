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
      colorClass: 'text-[#00B074]',
      bgClass: 'bg-[#EBFDF5]',
      borderClass: 'border-emerald-100',
      iconColor: '#00B074',
    },
    {
      title: 'Failed',
      value: failedCount,
      changeText: '-from this week',
      icon: AlertCircle,
      colorClass: 'text-[#FF4D4D]',
      bgClass: 'bg-[#FFF5F5]',
      borderClass: 'border-red-100',
      iconColor: '#FF4D4D',
    },
    {
      title: 'Total batches',
      value: totalBatches,
      changeText: '+from this week',
      icon: FileText,
      colorClass: 'text-[#5E40FF]',
      bgClass: 'bg-[#F5F3FF]',
      borderClass: 'border-indigo-100',
      iconColor: '#5E40FF',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6 w-full mb-8">
      {cards.map((card, idx) => {
        const Icon = card.icon;
        return (
          <div
            key={idx}
            className="bg-white border border-gray-150 rounded-2xl p-6 shadow-sm flex flex-col justify-between hover:shadow-md transition-shadow duration-150"
          >
            {/* Top row: Icon & Title */}
            <div className="flex items-center space-x-2.5 mb-3">
              <Icon className="w-5 h-5" style={{ color: card.iconColor }} />
              <span className={`font-bold text-[15px] ${card.colorClass}`}>
                {card.title}
              </span>
            </div>

            {/* Middle: Large Value */}
            <div className="mb-2">
              <span className={`text-[46px] font-bold leading-none tracking-tight ${card.colorClass}`}>
                {card.value}
              </span>
            </div>

            {/* Bottom: Subtext */}
            <div>
              <span className={`text-xs font-semibold ${card.colorClass}`}>
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
