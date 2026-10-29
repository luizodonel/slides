import React, { useState } from 'react';
import { X, ChevronLeft, ChevronRight, CreditCard, FileDown, CheckCircle2 } from 'lucide-react';
import SlideCanvas16x9 from './SlideCanvas16x9';

export default function PdfPreviewModal({
  isOpen,
  onClose,
  presentation,
  totalPages,
  primaryColor,
  secondaryColor,
  isPaid,
  onOpenPayment,
  onDownloadOfficial
}) {
  const [currentIdx, setCurrentIdx] = useState(0);

  if (!isOpen || !presentation || !presentation.slides) return null;

  const slides = presentation.slides;
  const currentSlide = slides[currentIdx] || slides[0];
  const priceFormatted = `R$ ${(totalPages * 1.5).toFixed(2).replace('.', ',')}`;

  return (
    <div className="edu-modal-backdrop" onClick={onClose}>
      <div className="edu-modal-card edu-pdf-preview-card" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="edu-modal-header">
          <div className="edu-modal-header-info">
            <div>
              <h2 className="edu-modal-title">Visualizar PDF (Prévia)</h2>
              <p className="edu-modal-sub">
                {isPaid
                  ? 'Apresentação oficial liberada sem marca d\'água'
                  : 'Documento em modo rascunho. Efetue o pagamento para baixar a versão oficial limpa.'}
              </p>
            </div>
          </div>
          <button type="button" className="edu-modal-close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        {/* Slide Preview with Watermark */}
        <div className="edu-pdf-preview-viewport">
          <div className="edu-pdf-slide-container">
            <SlideCanvas16x9
              slide={currentSlide}
              pageIndex={currentIdx}
              totalPages={slides.length}
              primaryColor={primaryColor}
              secondaryColor={secondaryColor}
              presentationTitle={presentation.presentationTitle}
              showWatermark={!isPaid}
            />
          </div>
        </div>

        {/* Pager and Actions */}
        <div className="edu-pdf-preview-footer">
          <div className="edu-pdf-pager">
            <button
              type="button"
              className="edu-stage-btn"
              disabled={currentIdx === 0}
              onClick={() => setCurrentIdx((i) => Math.max(0, i - 1))}
            >
              <ChevronLeft size={16} />
              <span>Anterior</span>
            </button>
            <span className="edu-stage-counter">
              Página <strong>{currentIdx + 1}</strong> de <strong>{slides.length}</strong>
            </span>
            <button
              type="button"
              className="edu-stage-btn"
              disabled={currentIdx >= slides.length - 1}
              onClick={() => setCurrentIdx((i) => Math.min(slides.length - 1, i + 1))}
            >
              <span>Próxima</span>
              <ChevronRight size={16} />
            </button>
          </div>

          <div className="edu-pdf-actions">
            {!isPaid ? (
              <button
                type="button"
                className="edu-btn-modal-action"
                style={{ backgroundColor: '#10b981' }}
                onClick={() => {
                  onClose();
                  onOpenPayment();
                }}
              >
                <CreditCard size={18} />
                <span>Pagar {priceFormatted} e Baixar PDF Oficial</span>
              </button>
            ) : (
              <button
                type="button"
                className="edu-btn-modal-action"
                style={{ backgroundColor: primaryColor }}
                onClick={() => {
                  onClose();
                  onDownloadOfficial();
                }}
              >
                <FileDown size={18} />
                <span>Baixar PDF Oficial (Sem Rascunho)</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
