import React, { useState } from 'react';
import { MessageCircle, X, ExternalLink, HelpCircle, PhoneCall } from 'lucide-react';

export default function WhatsAppSupport() {
  const [isOpen, setIsOpen] = useState(false);
  const phoneNumber = '5561985823912';
  const phoneFormatted = '(61) 9 8582-3912';
  const defaultMessage = 'Olá! Preciso de ajuda com meus slides no Slides da Oninha.';
  const whatsappUrl = `https://wa.me/${phoneNumber}?text=${encodeURIComponent(defaultMessage)}`;

  return (
    <>
      {/* Botão Flutuante no Canto Inferior Direito */}
      <div className="edu-whatsapp-fab-container">
        <button
          type="button"
          className="edu-whatsapp-fab"
          onClick={() => setIsOpen(!isOpen)}
          title={`Suporte WhatsApp ${phoneFormatted}`}
          aria-label="Suporte WhatsApp"
        >
          <svg
            className="edu-whatsapp-icon"
            viewBox="0 0 24 24"
            width="28"
            height="28"
            fill="currentColor"
          >
            <path d="M12.031 6.172c-3.181 0-5.767 2.586-5.768 5.766-.001 1.298.38 2.27 1.019 3.287l-.582 2.128 2.182-.573c.978.58 1.911.928 3.145.929 3.178 0 5.767-2.587 5.768-5.766.001-3.187-2.575-5.771-5.764-5.771zm3.392 8.244c-.144.405-.837.774-1.17.824-.299.045-.677.063-1.092-.069-.252-.08-.575-.187-.988-.365-1.739-.751-2.874-2.502-2.961-2.617-.087-.116-.708-.94-.708-1.793s.448-1.273.607-1.446c.159-.173.346-.217.462-.217l.332.006c.106.005.249-.04.39.298.144.347.491 1.2.534 1.287.043.087.072.188.014.304-.058.116-.087.188-.173.289l-.26.304c-.087.086-.177.18-.076.354.101.174.449.741.964 1.201.662.591 1.221.774 1.394.86.173.086.275.072.376-.044.101-.116.433-.506.549-.68.116-.173.231-.145.39-.087s1.011.477 1.184.564.289.13.332.202c.043.072.043.419-.101.824zM12 2C6.477 2 2 6.477 2 12c0 1.891.524 3.66 1.434 5.176L2 22l4.957-1.395A9.957 9.957 0 0012 22c5.523 0 10-4.477 10-10S17.523 2 12 2zm0 18c-1.61 0-3.11-.443-4.402-1.213l-.316-.188-2.943.829.837-2.873-.207-.33A7.954 7.954 0 014 12c0-4.411 3.589-8 8-8s8 3.589 8 8-3.589 8-8 8z" />
          </svg>
          <span className="edu-whatsapp-label">Suporte WhatsApp</span>
        </button>

        {/* Card Modal Rápido de Suporte */}
        {isOpen && (
          <div className="edu-whatsapp-card">
            <div className="edu-whatsapp-card-header">
              <div className="edu-whatsapp-header-content">
                <div className="edu-whatsapp-avatar-badge">
                  <PhoneCall size={18} color="#fff" />
                </div>
                <div>
                  <h4 className="edu-whatsapp-card-title">Central de Atendimento</h4>
                  <p className="edu-whatsapp-card-sub">Suporte Rápido e Humanizado</p>
                </div>
              </div>
              <button
                type="button"
                className="edu-whatsapp-close"
                onClick={() => setIsOpen(false)}
              >
                <X size={16} />
              </button>
            </div>

            <div className="edu-whatsapp-card-body">
              <p className="edu-whatsapp-desc">
                Precisa de ajuda para montar seus slides, tirar dúvidas sobre o pagamento Pix ou solicitar um tema especial? Fale conosco agora!
              </p>

              <div className="edu-whatsapp-number-box">
                <span className="edu-whatsapp-phone-label">WhatsApp Oficial:</span>
                <strong className="edu-whatsapp-phone-value">{phoneFormatted}</strong>
              </div>

              <a
                href={whatsappUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="edu-btn-open-whatsapp"
                onClick={() => setIsOpen(false)}
              >
                <span>Iniciar Conversa no WhatsApp</span>
                <ExternalLink size={16} />
              </a>
            </div>
          </div>
        )}
      </div>
    </>
  );
}
