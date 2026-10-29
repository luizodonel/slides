import React from 'react';

export default function SlideCanvas16x9({
  slide,
  pageIndex,
  totalPages,
  primaryColor = '#F97316',
  secondaryColor = '#1E293B',
  presentationTitle = 'APRESENTAÇÃO',
  showWatermark = false,
  isSubtle = false
}) {
  if (!slide) return null;

  const layout = slide.layoutType || 'split-photo-left';
  const pageLabel = `${String(pageIndex + 1).padStart(2, '0')}/${String(totalPages || 1).padStart(2, '0')}`;

  const renderPhotoFrame = (className = '', style = {}) => {
    if (slide.photoBase64) {
      return (
        <div className={`slide-photo-box has-photo ${className}`} style={style}>
          <img src={slide.photoBase64} alt={slide.photoCaption || 'Foto do slide'} />
          {slide.photoCaption && (
            <div className="slide-photo-caption">{slide.photoCaption}</div>
          )}
        </div>
      );
    }

    return (
      <div
        className={`slide-photo-box placeholder-photo ${className}`}
        style={{
          background: `linear-gradient(135deg, ${secondaryColor} 0%, ${primaryColor} 140%)`,
          ...style
        }}
      >
        <div className="geo-placeholder-decor">
          <div className="geo-circle-outer" style={{ borderColor: `${primaryColor}55` }} />
          <div className="geo-diamond" style={{ backgroundColor: primaryColor }} />
        </div>
        {slide.photoCaption && (
          <div className="slide-photo-caption">{slide.photoCaption}</div>
        )}
      </div>
    );
  };

  return (
    <div
      className={`slide-canvas-16x9 layout-${layout}`}
      style={{
        '--p-color': primaryColor,
        '--s-color': secondaryColor
      }}
    >
      {/* 1. COVER CIRCLE */}
      {layout === 'cover-circle' && (
        <div className="s-layout s-cover-circle">
          <div className="cc-left-bg" style={{ backgroundColor: primaryColor }}>
            <div className="cc-badge">
              <span className="cc-diamond" />
              <span>{slide.badge || 'APRESENTAÇÃO OFICIAL'}</span>
            </div>
            <h1 className="cc-title">{slide.title}</h1>
            <div className="cc-divider" style={{ backgroundColor: secondaryColor }} />
            <p className="cc-subtitle">{slide.subtitle}</p>

            {slide.bullets && slide.bullets.length > 0 && (
              <div className="cc-bullets">
                {slide.bullets.slice(0, 3).map((b, i) => (
                  <div key={i} className="cc-bullet-pill">
                    <span>▸</span> {b}
                  </div>
                ))}
              </div>
            )}

            <div className="cc-bottom-bars">
              <div style={{ backgroundColor: secondaryColor }} />
              <div style={{ backgroundColor: 'rgba(0,0,0,0.22)' }} />
              <div style={{ backgroundColor: 'rgba(255,255,255,0.25)' }} />
            </div>
          </div>

          <div className="cc-right-bg">
            <div className="cc-top-right-bar" style={{ backgroundColor: secondaryColor }} />
            <div className="cc-circle-wrapper" style={{ borderColor: secondaryColor }}>
              {renderPhotoFrame('cc-circle-photo')}
            </div>
          </div>
        </div>
      )}

      {/* 2. COVER DIAGONAL */}
      {layout === 'cover-diagonal' && (
        <div className="s-layout s-cover-diagonal">
          <div className="cd-left-content">
            <span className="cd-badge" style={{ backgroundColor: primaryColor }}>
              {slide.badge || 'APRESENTAÇÃO'}
            </span>
            <h1 className="cd-title" style={{ color: secondaryColor }}>{slide.title}</h1>
            <div className="cd-divider" style={{ backgroundColor: primaryColor }} />
            <p className="cd-subtitle">{slide.subtitle}</p>
            {slide.bullets && (
              <div className="cd-bullets">
                {slide.bullets.slice(0, 3).map((b, i) => (
                  <div key={i} className="cd-bullet-item">
                    <span className="cd-dot" style={{ backgroundColor: primaryColor }} />
                    <strong style={{ color: secondaryColor }}>{b}</strong>
                  </div>
                ))}
              </div>
            )}
          </div>
          <div className="cd-diagonal-stripe-secondary" style={{ backgroundColor: secondaryColor }} />
          <div className="cd-diagonal-stripe-primary" style={{ backgroundColor: primaryColor }} />
          <div className="cd-right-photo-clip">
            {renderPhotoFrame('cd-full-photo')}
          </div>
        </div>
      )}

      {/* 3. SPLIT PHOTO LEFT */}
      {layout === 'split-photo-left' && (
        <div className="s-layout s-split-left">
          <div className="spl-pillar" style={{ backgroundColor: primaryColor }} />
          <div className="spl-corner-top" style={{ backgroundColor: secondaryColor }} />
          <div className="spl-photo-container">
            {renderPhotoFrame('spl-photo')}
          </div>
          <div className="spl-content">
            <div className="spl-badge" style={{ color: primaryColor }}>{slide.badge}</div>
            <h2 className="spl-title" style={{ color: secondaryColor }}>{slide.title}</h2>
            <div className="spl-bar" style={{ backgroundColor: primaryColor }} />
            {slide.subtitle && <h3 className="spl-subtitle">{slide.subtitle}</h3>}
            {slide.bodyText && <p className="spl-body">{slide.bodyText}</p>}

            {slide.bullets && slide.bullets.length > 0 && (
              <div className="spl-bullets">
                {slide.bullets.slice(0, 3).map((b, i) => (
                  <div key={i} className="spl-bullet-row">
                    <span className="spl-num" style={{ backgroundColor: primaryColor }}>
                      0{i + 1}
                    </span>
                    <p>{b}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
          <div className="spl-bottom-accent" style={{ backgroundColor: secondaryColor }} />
        </div>
      )}

      {/* 4. SPLIT PHOTO RIGHT */}
      {layout === 'split-photo-right' && (
        <div className="s-layout s-split-right">
          <div className="spr-content">
            <div className="spr-badge" style={{ borderLeftColor: primaryColor, color: primaryColor }}>
              {slide.badge}
            </div>
            <h2 className="spr-title" style={{ color: secondaryColor }}>{slide.title}</h2>
            <div className="spr-bar" style={{ backgroundColor: primaryColor }} />
            {slide.bodyText && <p className="spr-body">{slide.bodyText}</p>}

            {slide.bullets && slide.bullets.length > 0 && (
              <div className="spr-cards">
                {slide.bullets.slice(0, 3).map((b, i) => (
                  <div
                    key={i}
                    className="spr-card-item"
                    style={{ borderLeftColor: i % 2 === 0 ? primaryColor : secondaryColor }}
                  >
                    {b}
                  </div>
                ))}
              </div>
            )}
          </div>
          <div className="spr-right-pillar" style={{ backgroundColor: primaryColor }} />
          <div className="spr-right-bottom" style={{ backgroundColor: secondaryColor }} />
          <div className="spr-photo-container">
            {renderPhotoFrame('spr-photo')}
          </div>
        </div>
      )}

      {/* 5. COLUMNS 4 (01, 02, 03, 04) */}
      {layout === 'columns-4' && (
        <div className="s-layout s-columns-4">
          <div className="c4-header" style={{ backgroundColor: secondaryColor, borderBottomColor: primaryColor }}>
            <div className="c4-header-text">
              <span className="c4-badge" style={{ color: primaryColor }}>{slide.badge}</span>
              <h2>{slide.title}</h2>
              <p>{slide.subtitle}</p>
            </div>
            {slide.photoBase64 && (
              <div className="c4-header-photo">
                <img src={slide.photoBase64} alt="Foto" />
              </div>
            )}
          </div>

          <div className="c4-grid">
            {(slide.columns || []).slice(0, 4).map((col, idx) => {
              const accent = idx % 2 === 0 ? primaryColor : secondaryColor;
              return (
                <div key={idx} className="c4-card">
                  <div className="c4-card-top" style={{ backgroundColor: accent }}>
                    <span className="c4-num">{col.number || `0${idx + 1}`}</span>
                  </div>
                  <div className="c4-card-body">
                    <h3 style={{ color: secondaryColor }}>{col.title}</h3>
                    <div className="c4-mini-bar" style={{ backgroundColor: primaryColor }} />
                    <p>{col.description}</p>
                  </div>
                  <div className="c4-card-foot" style={{ backgroundColor: accent }} />
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* 6. GRID 4 CARDS (2x2) */}
      {layout === 'grid-4-cards' && (
        <div className="s-layout s-grid-4">
          <div className="g4-banner" style={{ backgroundColor: primaryColor }}>
            <div className="g4-banner-left">
              <span className="g4-badge">{slide.badge}</span>
              <h2>{slide.title}</h2>
              <p>{slide.subtitle}</p>
            </div>
            <div className="g4-banner-right" style={{ backgroundColor: secondaryColor }}>
              {slide.photoBase64 && <img src={slide.photoBase64} alt="Capa" />}
            </div>
          </div>

          <div className="g4-matrix">
            {(slide.columns || []).slice(0, 4).map((item, idx) => {
              const badgeColor = idx === 0 || idx === 3 ? primaryColor : secondaryColor;
              return (
                <div key={idx} className="g4-box">
                  <div className="g4-icon-badge" style={{ backgroundColor: badgeColor }}>
                    {item.number || `0${idx + 1}`}
                  </div>
                  <div className="g4-box-text">
                    <h3 style={{ color: secondaryColor }}>{item.title}</h3>
                    <div className="g4-line" style={{ backgroundColor: primaryColor }} />
                    <p>{item.description}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* 7. INFOGRAPHIC STATS */}
      {layout === 'infographic-stats' && (
        <div className="s-layout s-infographic">
          <div className="info-left" style={{ backgroundColor: secondaryColor }}>
            <span className="info-badge" style={{ color: primaryColor }}>{slide.badge}</span>
            <h2 className="info-title">{slide.title}</h2>
            <p className="info-body">{slide.bodyText}</p>

            {slide.photoBase64 ? (
              <div className="info-photo-box">
                <img src={slide.photoBase64} alt="Gráfico / Foto" />
              </div>
            ) : (
              <div className="info-bar-chart">
                {[45, 72, 58, 88, 74, 96].map((val, i) => (
                  <div key={i} className="info-bar-col">
                    <div
                      className="info-bar-fill"
                      style={{
                        height: `${val}%`,
                        backgroundColor: i % 2 === 0 ? '#ffffff' : primaryColor
                      }}
                    />
                    <span>P{i + 1}</span>
                  </div>
                ))}
              </div>
            )}

            <div className="info-bottom-stripes">
              <div style={{ backgroundColor: primaryColor }} />
              <div style={{ backgroundColor: 'rgba(255,255,255,0.25)' }} />
              <div style={{ backgroundColor: 'rgba(255,255,255,0.1)' }} />
            </div>
          </div>

          <div className="info-right">
            {(slide.stats || []).slice(0, 3).map((st, idx) => (
              <div key={idx} className="info-stat-row">
                <div
                  className="info-donut"
                  style={{
                    borderColor: idx % 2 === 0 ? primaryColor : secondaryColor
                  }}
                >
                  <span style={{ color: secondaryColor }}>{st.value}</span>
                </div>
                <div className="info-stat-text">
                  <strong className="info-stat-val" style={{ color: primaryColor }}>{st.value}</strong>
                  <h4 style={{ color: secondaryColor }}>{st.label}</h4>
                  <p>{st.description}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 8. TIMELINE STEPS */}
      {layout === 'timeline-steps' && (
        <div className="s-layout s-timeline">
          <div className="tl-left-strip" style={{ backgroundColor: primaryColor }} />
          <div className="tl-top-row">
            <div>
              <span className="tl-badge" style={{ color: primaryColor }}>{slide.badge}</span>
              <h2 className="tl-title" style={{ color: secondaryColor }}>{slide.title}</h2>
              <p className="tl-subtitle">{slide.subtitle}</p>
            </div>
            {slide.photoBase64 && (
              <div className="tl-mini-photo" style={{ borderColor: secondaryColor }}>
                <img src={slide.photoBase64} alt="Etapa" />
              </div>
            )}
          </div>

          <div className="tl-track-wrapper">
            <div className="tl-line" />
            <div className="tl-steps-grid">
              {(slide.columns || []).slice(0, 4).map((step, idx) => {
                const color = idx % 2 === 0 ? primaryColor : secondaryColor;
                return (
                  <div key={idx} className="tl-step-col">
                    <div className="tl-node" style={{ backgroundColor: color }}>
                      0{idx + 1}
                    </div>
                    <div className="tl-card" style={{ borderTopColor: color }}>
                      <h3 style={{ color: secondaryColor }}>{step.title}</h3>
                      <p>{step.description}</p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* 9. QUOTE & VISION / MISSION */}
      {layout === 'quote-vision' && (
        <div className="s-layout s-quote-vision">
          <div className="qv-left-bg">
            {slide.photoBase64 && <img src={slide.photoBase64} alt="Fundo" className="qv-bg-img" />}
            <div className="qv-quote-card" style={{ backgroundColor: primaryColor }}>
              <div className="qv-quote-mark" style={{ color: secondaryColor }}>“</div>
              <p className="qv-quote-text">{slide.highlightQuote || slide.bodyText}</p>
            </div>
          </div>

          <div className="qv-right-content">
            <span className="qv-badge" style={{ color: primaryColor }}>{slide.badge}</span>
            <h2 className="qv-title" style={{ color: secondaryColor }}>{slide.title}</h2>

            <div className="qv-boxes">
              {(slide.columns || []).slice(0, 2).map((col, idx) => (
                <div
                  key={idx}
                  className={`qv-box ${idx === 0 ? 'qv-dark' : 'qv-light'}`}
                  style={{
                    backgroundColor: idx === 0 ? secondaryColor : '#f8fafc',
                    borderTopColor: primaryColor
                  }}
                >
                  <h3 style={{ color: idx === 0 ? primaryColor : secondaryColor }}>{col.title}</h3>
                  <div className="qv-small-line" style={{ backgroundColor: primaryColor }} />
                  <p style={{ color: idx === 0 ? '#e2e8f0' : '#475569' }}>{col.description}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* 10. CLOSING THANK YOU */}
      {layout === 'closing-thankyou' && (
        <div className="s-layout s-closing" style={{ backgroundColor: secondaryColor }}>
          <div className="cl-left">
            <span className="cl-badge" style={{ color: primaryColor }}>{slide.badge}</span>
            <h1 className="cl-title">{slide.title}</h1>
            <div className="cl-bar" style={{ backgroundColor: primaryColor }} />
            <p className="cl-subtitle">{slide.subtitle}</p>
            <p className="cl-body">{slide.bodyText}</p>

            {slide.bullets && (
              <div className="cl-bullets">
                {slide.bullets.slice(0, 3).map((b, i) => (
                  <div key={i} className="cl-bullet">
                    <span className="cl-dot" style={{ backgroundColor: primaryColor }} />
                    <span>{b}</span>
                  </div>
                ))}
              </div>
            )}

            <div className="cl-bottom-blocks">
              <div style={{ backgroundColor: primaryColor }} />
              <div style={{ backgroundColor: 'rgba(0,0,0,0.28)' }} />
              <div style={{ backgroundColor: 'rgba(255,255,255,0.25)' }} />
            </div>
          </div>

          <div className="cl-right">
            <div className="cl-photo-top-bars">
              <div style={{ backgroundColor: primaryColor }} />
              <div style={{ backgroundColor: 'rgba(255,255,255,0.3)' }} />
            </div>
            <div className="cl-photo-frame">
              {renderPhotoFrame('cl-photo')}
            </div>
          </div>
        </div>
      )}

      {/* Slide Footer Page Counter */}
      <div className="slide-canvas-footer">
        {layout !== 'cover-circle' && layout !== 'closing-thankyou' && layout !== 'infographic-stats' && (
          <span className="scf-title">{presentationTitle}</span>
        )}
        <span className="scf-page-badge" style={{ backgroundColor: primaryColor }}>
          {pageLabel}
        </span>
      </div>

      {/* OVERLAY RASCUNHO (MOSTRADO NA TELA CLARINHO OU NO MODAL PDF) */}
      {showWatermark && (
        <div className={`slide-anti-fraud-overlay ${isSubtle ? 'is-subtle' : ''}`}>
          <div className="saf-watermark-grid">
            {Array.from({ length: 6 }).map((_, i) => (
              <span key={i} className="saf-repeat-text">
                RASCUNHO • SLIDES DA ONINHA • PRÉ-VISUALIZAÇÃO
              </span>
            ))}
          </div>

          <div className="saf-central-banner">
            <div className="saf-banner-badge">PRÉVIA DO DOCUMENTO</div>
            <div className="saf-banner-title">RASCUNHO</div>
            <div className="saf-banner-sub">
              SLIDES DA ONINHA — PRÉ-VISUALIZAÇÃO DO PDF
            </div>
            <div className="saf-banner-legal">
              BAIXE O ARQUIVO OFICIAL EM ALTA DEFINIÇÃO APÓS A LIBERAÇÃO
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

