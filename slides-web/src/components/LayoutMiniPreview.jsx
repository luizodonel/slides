import React from 'react';

export default function LayoutMiniPreview({ layoutId, primary = '#F97316', secondary = '#1E293B' }) {
  switch (layoutId) {
    case 'cover-circle':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <rect x="0" y="0" width="98" height="90" fill={primary} />
          <rect x="0" y="76" width="32" height="14" fill={secondary} />
          <rect x="32" y="76" width="32" height="14" fill="#00000033" />
          <rect x="118" y="0" width="42" height="10" fill={secondary} />
          <circle cx="105" cy="44" r="28" fill="#ffffff" />
          <circle cx="105" cy="44" r="24" fill={secondary} />
          <rect x="12" y="24" width="56" height="8" rx="2" fill="#ffffff" />
          <rect x="12" y="36" width="46" height="6" rx="2" fill="#ffffffcc" />
          <rect x="12" y="50" width="52" height="4" rx="1" fill="#ffffff99" />
        </svg>
      );

    case 'cover-diagonal':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <polygon points="82,0 160,0 160,90 112,90" fill={secondary} />
          <polygon points="72,0 84,0 114,90 102,90" fill={primary} />
          <rect x="12" y="16" width="34" height="6" rx="2" fill={primary} />
          <rect x="12" y="30" width="56" height="9" rx="2" fill={secondary} />
          <rect x="12" y="43" width="44" height="7" rx="2" fill={secondary} />
          <rect x="12" y="58" width="48" height="4" rx="1" fill="#94a3b8" />
        </svg>
      );

    case 'split-photo-left':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <rect x="14" y="0" width="36" height="90" fill={primary} />
          <rect x="10" y="14" width="54" height="60" fill={secondary} stroke="#ffffff" strokeWidth="2" />
          <rect x="76" y="16" width="68" height="8" rx="2" fill={secondary} />
          <rect x="76" y="30" width="62" height="4" rx="1" fill="#94a3b8" />
          <rect x="76" y="38" width="58" height="4" rx="1" fill="#94a3b8" />
          <rect x="76" y="52" width="10" height="8" rx="2" fill={primary} />
          <rect x="90" y="54" width="52" height="4" rx="1" fill={secondary} />
          <rect x="76" y="64" width="10" height="8" rx="2" fill={primary} />
          <rect x="90" y="66" width="52" height="4" rx="1" fill={secondary} />
        </svg>
      );

    case 'split-photo-right':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <rect x="108" y="0" width="52" height="90" fill={primary} />
          <rect x="92" y="14" width="56" height="58" fill={secondary} stroke="#ffffff" strokeWidth="2" />
          <rect x="12" y="16" width="66" height="8" rx="2" fill={secondary} />
          <rect x="12" y="30" width="68" height="4" rx="1" fill="#94a3b8" />
          <rect x="12" y="44" width="68" height="12" rx="2" fill="#f1f5f9" />
          <rect x="12" y="44" width="4" height="12" fill={primary} />
          <rect x="12" y="60" width="68" height="12" rx="2" fill="#f1f5f9" />
          <rect x="12" y="60" width="4" height="12" fill={secondary} />
        </svg>
      );

    case 'columns-4':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <rect x="0" y="0" width="160" height="22" fill={secondary} />
          {[0, 1, 2, 3].map((i) => (
            <g key={i}>
              <rect x={10 + i * 36} y="28" width="32" height="54" rx="3" fill="#f1f5f9" />
              <rect x={10 + i * 36} y="28" width="32" height="16" rx="3" fill={i % 2 === 0 ? primary : secondary} />
            </g>
          ))}
        </svg>
      );

    case 'grid-4-cards':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <rect x="0" y="0" width="160" height="22" fill={primary} />
          {[0, 1].map((r) =>
            [0, 1].map((c) => (
              <g key={`${r}-${c}`}>
                <rect x={12 + c * 70} y={28 + r * 28} width="64" height="24" rx="3" fill="#f1f5f9" />
                <rect x={16 + c * 70} y={32 + r * 28} width="12" height="12" rx="2" fill={(r + c) % 2 === 0 ? primary : secondary} />
              </g>
            ))
          )}
        </svg>
      );

    case 'infographic-stats':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <rect x="0" y="0" width="62" height="90" fill={secondary} />
          <rect x="8" y="22" width="46" height="8" rx="2" fill="#ffffff" />
          <rect x="8" y="34" width="38" height="5" rx="1" fill="#ffffffaa" />
          <rect x="8" y="44" width="42" height="4" rx="1" fill="#ffffff66" />
          <rect x="8" y="74" width="46" height="4" rx="1" fill={primary} />
          {[0, 1, 2].map((i) => (
            <g key={i}>
              <rect x="70" y={14 + i * 24} width="82" height="20" rx="3" fill="#f8fafc" stroke="#e2e8f0" strokeWidth="1" />
              <circle cx="82" cy={24 + i * 24} r="6" fill={i % 2 === 0 ? primary : secondary} />
              <rect x="94" y={19 + i * 24} width="36" height="4" rx="1" fill={secondary} />
              <rect x="94" y={25 + i * 24} width="50" height="3" rx="1" fill="#94a3b8" />
            </g>
          ))}
        </svg>
      );

    case 'timeline-steps':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <line x1="20" y1="36" x2="140" y2="36" stroke="#cbd5e1" strokeWidth="3" />
          {[0, 1, 2, 3].map((i) => (
            <g key={i}>
              <circle cx={26 + i * 36} cy="36" r="8" fill={i % 2 === 0 ? primary : secondary} />
              <rect x={12 + i * 36} y="48" width="28" height="32" rx="3" fill="#f1f5f9" />
            </g>
          ))}
        </svg>
      );

    case 'quote-vision':
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill="#ffffff" />
          <rect x="0" y="0" width="66" height="90" fill="#f1f5f9" />
          <rect x="10" y="14" width="62" height="62" rx="4" fill={primary} />
          <rect x="82" y="26" width="32" height="50" rx="3" fill={secondary} />
          <rect x="118" y="26" width="32" height="50" rx="3" fill="#f1f5f9" />
        </svg>
      );

    case 'closing-thankyou':
    default:
      return (
        <svg viewBox="0 0 160 90" className="layout-mini-svg">
          <rect width="160" height="90" fill={secondary} />
          <rect x="0" y="74" width="32" height="16" fill={primary} />
          <rect x="32" y="74" width="32" height="16" fill="#ffffff44" />
          <rect x="104" y="14" width="44" height="54" fill="#ffffff" />
          <rect x="106" y="16" width="40" height="50" fill={primary} />
          <rect x="14" y="28" width="72" height="10" rx="2" fill="#ffffff" />
          <rect x="14" y="44" width="52" height="5" rx="1" fill={primary} />
        </svg>
      );
  }
}

