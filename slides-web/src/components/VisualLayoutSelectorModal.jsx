import React, { useEffect } from 'react';
import { X, Check } from 'lucide-react';
import { PAGE_LAYOUTS } from '../data/slideTemplates';
import LayoutMiniPreview from './LayoutMiniPreview';

export default function VisualLayoutSelectorModal({
  isOpen,
  onClose,
  pageIndex,
  currentLayout,
  onSelectLayout,
  primaryColor = '#2563EB',
  secondaryColor = '#0F172A'
}) {
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleKeyDown);
    }
    return () => {
      document.body.style.overflow = '';
      window.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="edu-modal-backdrop" onClick={onClose}>
      <div
        className="edu-layout-selector-dialog"
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
      >
        {/* Header */}
        <div className="edu-layout-modal-header">
          <div>
            <h2 className="edu-layout-modal-title">
              Escolher Modelo do Slide • Página {pageIndex + 1}
            </h2>
            <p className="edu-layout-modal-sub">
              Toque no modelo desejado para aplicar à página:
            </p>
          </div>
          <button
            type="button"
            className="edu-layout-modal-close"
            onClick={onClose}
            title="Fechar"
          >
            <X size={20} />
          </button>
        </div>

        {/* Layout Cards Grid */}
        <div className="edu-layout-modal-body">
          <div className="edu-layout-grid-picker">
            {PAGE_LAYOUTS.map((layout) => {
              const isSelected = layout.id === currentLayout;
              return (
                <div
                  key={layout.id}
                  className={`edu-layout-card-option ${isSelected ? 'is-selected' : ''}`}
                  onClick={() => {
                    onSelectLayout(layout.id);
                    onClose();
                  }}
                >
                  <div className="edu-layout-card-thumb">
                    <LayoutMiniPreview
                      layoutId={layout.id}
                      primary={primaryColor}
                      secondary={secondaryColor}
                    />
                    {isSelected && (
                      <span className="edu-layout-active-badge">
                        <Check size={12} strokeWidth={3} /> Modelo Ativo
                      </span>
                    )}
                  </div>

                  <div className="edu-layout-card-info">
                    <div className="edu-layout-card-header-row">
                      <strong className="edu-layout-card-title">{layout.name}</strong>
                      <span className="edu-layout-cat-pill">{layout.category}</span>
                    </div>
                    <p className="edu-layout-card-desc">{layout.description}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
