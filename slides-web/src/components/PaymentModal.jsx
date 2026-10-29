import React, { useState, useEffect } from 'react';
import { CheckCircle2, Copy, Check, QrCode, Sparkles, X, ShieldAlert, ArrowRight, Loader2 } from 'lucide-react';

export default function PaymentModal({
  isOpen,
  onClose,
  totalPages,
  onPaymentSuccess,
  primaryColor = '#2563eb'
}) {
  const [step, setStep] = useState('summary'); // 'summary' | 'qrcode' | 'success'
  const [payerName, setPayerName] = useState('');
  const [payerEmail, setPayerEmail] = useState('');
  const [payerCpf, setPayerCpf] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState(null);

  const [pedidoData, setPedidoData] = useState(null);
  const [copied, setCopied] = useState(false);
  const [isPolling, setIsPolling] = useState(false);

  const pricePerSlide = 1.5;
  const totalAmount = (totalPages * pricePerSlide).toFixed(2);
  const totalAmountFormatted = `R$ ${totalAmount.replace('.', ',')}`;

  // Reset when opened
  useEffect(() => {
    if (isOpen) {
      setStep('summary');
      setErrorMsg(null);
      setCopied(false);
      setIsPolling(false);
    }
  }, [isOpen]);

  // Polling for payment approval
  useEffect(() => {
    let intervalId = null;

    if (isPolling && pedidoData?.pedidoId && step === 'qrcode') {
      intervalId = setInterval(async () => {
        try {
          const res = await fetch(`/api/pagamento/status/${pedidoData.pedidoId}`);
          if (res.ok) {
            const data = await res.json();
            if (data.approved || data.status === 'APPROVED') {
              clearInterval(intervalId);
              setIsPolling(false);
              setStep('success');
              if (onPaymentSuccess) {
                onPaymentSuccess(pedidoData.pedidoId);
              }
            }
          }
        } catch (_) {
          // ignore transient poll error
        }
      }, 3000);
    }

    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [isPolling, pedidoData, step, onPaymentSuccess]);

  if (!isOpen) return null;

  const handleGeneratePix = async () => {
    setIsLoading(true);
    setErrorMsg(null);

    try {
      const res = await fetch('/api/pagamento/pix', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          totalSlides: totalPages,
          payerName: payerName || 'Estudante',
          payerEmail: payerEmail || `estudante.${Date.now()}@slidesdaanna.com.br`,
          payerCpf: payerCpf || '00000000000'
        })
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setPedidoData(data);
      setStep('qrcode');
      setIsPolling(true);
    } catch (err) {
      setErrorMsg('Não foi possível gerar a cobrança Pix no momento. Tente novamente.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCopyPix = () => {
    if (!pedidoData?.qrCode) return;
    navigator.clipboard.writeText(pedidoData.qrCode);
    setCopied(true);
    setTimeout(() => setCopied(false), 2500);
  };

  const handleSimulateApproval = async () => {
    if (!pedidoData?.pedidoId) return;
    setIsLoading(true);
    try {
      const res = await fetch(`/api/pagamento/simular-aprovacao/${pedidoData.pedidoId}`, {
        method: 'POST'
      });
      if (res.ok) {
        setIsPolling(false);
        setStep('success');
        if (onPaymentSuccess) {
          onPaymentSuccess(pedidoData.pedidoId);
        }
      }
    } catch (_) {
      // fallback
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="edu-modal-backdrop" onClick={onClose}>
      <div className="edu-modal-card" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="edu-modal-header">
          <div className="edu-modal-header-info">
            <div className="edu-modal-icon-badge" style={{ backgroundColor: primaryColor }}>
              <QrCode size={20} color="#fff" />
            </div>
            <div>
              <h2 className="edu-modal-title">Liberar Slides da Anna</h2>
              <p className="edu-modal-sub">Liberação imediata via Pix sem marca d'água</p>
            </div>
          </div>
          <button type="button" className="edu-modal-close" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        {/* Modal Body */}
        <div className="edu-modal-body">
          {errorMsg && (
            <div className="edu-modal-alert">
              <ShieldAlert size={16} />
              <span>{errorMsg}</span>
            </div>
          )}

          {/* STEP 1: SUMMARY & DETAILS */}
          {step === 'summary' && (
            <div className="edu-modal-step">
              <div className="edu-price-summary-box">
                <div className="edu-price-row">
                  <span>Quantidade de Páginas:</span>
                  <strong>{totalPages} slides</strong>
                </div>
                <div className="edu-price-row">
                  <span>Preço por Página:</span>
                  <span>R$ 1,50</span>
                </div>
                <div className="edu-price-divider" />
                <div className="edu-price-row total">
                  <span>Total a Pagar:</span>
                  <span className="edu-price-highlight" style={{ color: primaryColor }}>
                    {totalAmountFormatted}
                  </span>
                </div>
              </div>

              <div className="edu-benefits-list">
                <div className="edu-benefit-item">
                  <CheckCircle2 size={16} className="text-emerald-400" />
                  <span>Download do PDF Oficial em 16:9 Full HD</span>
                </div>
                <div className="edu-benefit-item">
                  <CheckCircle2 size={16} className="text-emerald-400" />
                  <span>Remoção instantânea do aviso "RASCUNHO"</span>
                </div>
                <div className="edu-benefit-item">
                  <CheckCircle2 size={16} className="text-emerald-400" />
                  <span>Ideal para apresentar na escola, faculdade ou trabalho</span>
                </div>
              </div>

              <div className="edu-inputs-group">
                <label className="edu-input-label">Seu Nome (opcional):</label>
                <input
                  type="text"
                  className="edu-modal-input"
                  placeholder="Ex: João da Silva"
                  value={payerName}
                  onChange={(e) => setPayerName(e.target.value)}
                />

                <label className="edu-input-label">Seu E-mail (opcional para recibo):</label>
                <input
                  type="email"
                  className="edu-modal-input"
                  placeholder="Ex: seuemail@gmail.com"
                  value={payerEmail}
                  onChange={(e) => setPayerEmail(e.target.value)}
                />
              </div>

              <button
                type="button"
                className="edu-btn-modal-action"
                style={{ backgroundColor: primaryColor }}
                onClick={handleGeneratePix}
                disabled={isLoading}
              >
                {isLoading ? (
                  <>
                    <Loader2 size={16} className="animate-spin" />
                    <span>Gerando Pix...</span>
                  </>
                ) : (
                  <>
                    <span>Pagar {totalAmountFormatted} via Pix</span>
                    <ArrowRight size={16} />
                  </>
                )}
              </button>
            </div>
          )}

          {/* STEP 2: QR CODE & COPIA E COLA */}
          {step === 'qrcode' && pedidoData && (
            <div className="edu-modal-step qrcode-step">
              <div className="edu-qr-frame">
                {pedidoData.qrCodeBase64 ? (
                  <img
                    src={`data:image/png;base64,${pedidoData.qrCodeBase64}`}
                    alt="QR Code Pix"
                    className="edu-qr-img"
                  />
                ) : (
                  <div className="edu-qr-placeholder">
                    <QrCode size={120} />
                  </div>
                )}
              </div>

              <div className="edu-qr-instructions">
                <strong>Abra o app do seu banco e aponte a câmera</strong>
                <p>ou copie o código Pix abaixo para pagar no seu aplicativo:</p>
              </div>

              {pedidoData.qrCode && (
                <div className="edu-pix-copy-box">
                  <input
                    type="text"
                    readOnly
                    value={pedidoData.qrCode}
                    className="edu-pix-code-input"
                  />
                  <button
                    type="button"
                    className={`edu-btn-copy-pix ${copied ? 'copied' : ''}`}
                    onClick={handleCopyPix}
                  >
                    {copied ? (
                      <>
                        <Check size={14} />
                        <span>Copiado!</span>
                      </>
                    ) : (
                      <>
                        <Copy size={14} />
                        <span>Copiar Chave</span>
                      </>
                    )}
                  </button>
                </div>
              )}

              <div className="edu-polling-status">
                <Loader2 size={16} className="animate-spin text-sky-400" />
                <span>Aguardando confirmação do banco... (atualização automática)</span>
              </div>

              {/* Modo de teste / aprovação rápida */}
              <div className="edu-test-approval-box">
                <button
                  type="button"
                  className="edu-btn-sim-test"
                  onClick={handleSimulateApproval}
                  disabled={isLoading}
                >
                  ⚡ Confirmar Pagamento Imediato (Modo Rápido / Teste)
                </button>
              </div>
            </div>
          )}

          {/* STEP 3: SUCCESS */}
          {step === 'success' && (
            <div className="edu-modal-step success-step">
              <div className="edu-success-badge">
                <CheckCircle2 size={48} color="#10b981" />
              </div>
              <h3 className="edu-success-title">Pagamento Confirmado!</h3>
              <p className="edu-success-sub">
                Seus slides foram liberados com sucesso sem marca d'água de rascunho. O download em PDF Full HD começou automaticamente!
              </p>

              <button
                type="button"
                className="edu-btn-modal-action"
                style={{ backgroundColor: '#10b981' }}
                onClick={onClose}
              >
                Concluir e Ver Meus Slides
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

