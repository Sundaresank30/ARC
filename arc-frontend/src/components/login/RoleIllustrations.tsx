import React from 'react';

export const ManagerIllustration: React.FC<{ className?: string }> = ({ className = "h-44 w-auto" }) => {
  return (
    <div className={`relative flex items-center justify-center ${className}`}>
      {/* Fallback image with clean SVG background illustration matching second reference image */}
      <img 
        src="/assets/manager.png" 
        alt="Manager Illustration" 
        className="h-full w-auto object-contain max-h-44 drop-shadow-sm"
        onError={(e) => {
          // If PNG fails to load for any reason, show fallback SVG illustration
          e.currentTarget.style.display = 'none';
        }}
      />
      
      {/* Dynamic Inline Vector Illustration as backup/complement */}
      <svg className="h-44 w-auto hidden" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
        {/* Soft shadow ground */}
        <ellipse cx="100" cy="180" rx="45" ry="5" fill="#E2E8F0" />
        {/* Manager Character */}
        <rect x="82" y="105" width="15" height="70" rx="7" fill="#D97706" />
        <rect x="103" y="105" width="15" height="70" rx="7" fill="#D97706" />
        <path d="M75 60 C75 55 125 55 125 60 L120 110 L80 110 Z" fill="#2563EB" />
        <circle cx="100" cy="38" r="16" fill="#FDE047" />
        <rect x="110" y="70" width="22" height="30" rx="3" fill="#E2E8F0" stroke="#475569" strokeWidth="2" />
      </svg>
    </div>
  );
};

export const OperatorIllustration: React.FC<{ className?: string }> = ({ className = "h-44 w-auto" }) => {
  return (
    <div className={`relative flex items-center justify-center ${className}`}>
      <img 
        src="/assets/operator.png" 
        alt="Operator Illustration" 
        className="h-full w-auto object-contain max-h-44 drop-shadow-sm"
        onError={(e) => {
          e.currentTarget.style.display = 'none';
        }}
      />
    </div>
  );
};
