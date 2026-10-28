import React, { useState, useEffect } from 'react';
import {
  Sparkles,
  FileDown,
  Layers,
  Palette,
  ImagePlus,
  Trash2,
  ArrowUp,
  ArrowDown,
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
  Wand2,
  BookOpen,
  Sliders,
  Maximize2,
  Eye,
  SlidersHorizontal
} from 'lucide-react';
import {
  SUBJECT_THEMES,
  COLOR_PALETTES,
  PAGE_LAYOUTS
} from './data/slideTemplates';
import LayoutMiniPreview from './components/LayoutMiniPreview';
import SlideCanvas16x9 from './components/SlideCanvas16x9';

const DEFAULT_LAYOUT_ORDER = [
  'cover-circle',
  'split-photo-left',
  'columns-4',
  'infographic-stats',
  'grid-4-cards',
  'closing-thankyou'
];

export default function App() {
  // Mobile/Tablet View Mode: 'edit' | 'preview'
  const [mobileView, setMobileView] = useState('preview');

  // 1. Idea & Subject
  const [idea, setIdea] = useState(
    'Teorema de Pitágoras e relações no triângulo retângulo aplicadas na arquitetura'
  );
  const [subjectTheme, setSubjectTheme] = useState('matematica');

  // 2. Colors (Auto AI vs Manual 2 Colors)
  const [autoColors, setAutoColors] = useState(true);
  const [primaryColor, setPrimaryColor] = useState('#2563EB');
  const [secondaryColor, setSecondaryColor] = useState('#0F172A');

  // 3. Pages & Layouts
  const [totalPages, setTotalPages] = useState(4);
  const [pageConfigs, setPageConfigs] = useState(() =>
    Array.from({ length: 4 }, (_, i) => ({
      pageNumber: i + 1,
      layoutType: i === 0 ? 'cover-circle' : i === 3 ? 'closing-thankyou' : DEFAULT_LAYOUT_ORDER[i % DEFAULT_LAYOUT_ORDER.length],
      customTitle: ''
    }))
  );

  // 4. Photos
  const [uploadedPhotos, setUploadedPhotos] = useState([]);

  // 5. Active Presentation State
  const [presentation, setPresentation] = useState(null);
  const [activeSlideIdx, setActiveSlideIdx] = useState(0);
  const [isGenerating, setIsGenerating] = useState(false);
  const [isExportingPdf, setIsExportingPdf] = useState(false);
  const [activeTab, setActiveTab] = useState('ideia'); // 'ideia' | 'paginas' | 'fotos' | 'cores'
  const [fullscreenMode, setFullscreenMode] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');

  // Synchronize page configs when total pages change
  const handleTotalPagesChange = (newTotal) => {
    const clamped = Math.max(2, Math.min(12, Number(newTotal) || 4));
    setTotalPages(clamped);

    setPageConfigs((prev) => {
      const next = [];
      for (let i = 0; i < clamped; i++) {
        if (i < prev.length) {
          next.push({ ...prev[i], pageNumber: i + 1 });
        } else {
          const defaultLayout =
            i === 0
              ? 'cover-circle'
              : i === clamped - 1
              ? 'closing-thankyou'
              : DEFAULT_LAYOUT_ORDER[i % DEFAULT_LAYOUT_ORDER.length];
          next.push({
            pageNumber: i + 1,
            layoutType: defaultLayout,
            customTitle: ''
          });
        }
      }
      return next;
    });
  };

  const updatePageLayout = (index, layoutType) => {
    setPageConfigs((prev) => {
      const copy = [...prev];
      copy[index] = { ...copy[index], layoutType };
      return copy;
    });

    if (presentation && presentation.slides && presentation.slides[index]) {
      setPresentation((prev) => {
        const slidesCopy = [...prev.slides];
        slidesCopy[index] = { ...slidesCopy[index], layoutType };
        return { ...prev, slides: slidesCopy };
      });
    }
  };

  const movePagePosition = (index, direction) => {
    const targetIdx = index + direction;
    if (targetIdx < 0 || targetIdx >= pageConfigs.length) return;

    const pageA = index + 1;
    const pageB = targetIdx + 1;

    setPageConfigs((prev) => {
      const copy = [...prev];
      const temp = copy[index];
      copy[index] = { ...copy[targetIdx], pageNumber: pageA };
      copy[targetIdx] = { ...temp, pageNumber: pageB };
      return copy;
    });

    setUploadedPhotos((prev) =>
      prev.map((p) => {
        if (p.pageNumber === pageA) return { ...p, pageNumber: pageB };
        if (p.pageNumber === pageB) return { ...p, pageNumber: pageA };
        return p;
      })
    );
  };

  const handlePhotoUpload = (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    files.forEach((file, idx) => {
      const reader = new FileReader();
      reader.onload = () => {
        const base64 = reader.result;
        setUploadedPhotos((prev) => {
          const usedPages = new Set(prev.map((p) => p.pageNumber));
          let defaultPage = 1;
          for (let p = 1; p <= totalPages; p++) {
            if (!usedPages.has(p)) {
              defaultPage = p;
              break;
            }
          }
          const newPhoto = {
            id: `${Date.now()}-${idx}-${Math.random().toString(36).slice(2, 6)}`,
            name: file.name,
            dataUrl: base64,
            pageNumber: defaultPage,
            caption: ''
          };
          const updated = [...prev, newPhoto];
          syncPhotosToLivePresentation(updated);
          return updated;
        });
      };
      reader.readAsDataURL(file);
    });
    e.target.value = '';
  };

  const updatePhotoAssignment = (photoId, field, value) => {
    setUploadedPhotos((prev) => {
      const updated = prev.map((item) =>
        item.id === photoId
          ? { ...item, [field]: field === 'pageNumber' ? Number(value) : value }
          : item
      );
      syncPhotosToLivePresentation(updated);
      return updated;
    });
  };

  const removePhoto = (photoId) => {
    setUploadedPhotos((prev) => {
      const updated = prev.filter((item) => item.id !== photoId);
      syncPhotosToLivePresentation(updated);
      return updated;
    });
  };

  const syncPhotosToLivePresentation = (photosList) => {
    setPresentation((prev) => {
      if (!prev || !prev.slides) return prev;
      const slidesCopy = prev.slides.map((s, i) => {
        const pageNum = i + 1;
        const assigned = photosList.find((p) => Number(p.pageNumber) === pageNum);
        return {
          ...s,
          photoBase64: assigned ? assigned.dataUrl : null,
          photoCaption: assigned ? assigned.caption : null
        };
      });
      return { ...prev, slides: slidesCopy };
    });
  };

  const getEffectiveColors = () => {
    if (!autoColors) {
      return [primaryColor, secondaryColor];
    }
    const foundTheme = SUBJECT_THEMES.find((t) => t.id === subjectTheme);
    return foundTheme ? foundTheme.defaultColors : ['#2563EB', '#0F172A'];
  };

  const [effectivePrimary, effectiveSecondary] = getEffectiveColors();

  const handleGenerateSlides = async () => {
    setIsGenerating(true);
    setStatusMessage('Criando slides educativos...');

    const pagesPayload = pageConfigs.map((cfg, idx) => {
      const pageNum = idx + 1;
      const assignedPhoto = uploadedPhotos.find((p) => Number(p.pageNumber) === pageNum);
      return {
        pageNumber: pageNum,
        layoutType: cfg.layoutType,
        customTitle: cfg.customTitle || null,
        photoBase64: assignedPhoto ? assignedPhoto.dataUrl : null,
        photoCaption: assignedPhoto ? assignedPhoto.caption : null
      };
    });

    const payload = {
      idea,
      subjectTheme,
      templateStyle: 'academic-clean',
      totalPages,
      autoColors,
      primaryColor: autoColors ? effectivePrimary : primaryColor,
      secondaryColor: autoColors ? effectiveSecondary : secondaryColor,
      pages: pagesPayload
    };

    try {
      const res = await fetch('/api/slides/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      setPresentation(data);
      setActiveSlideIdx(0);
      setStatusMessage('Slides gerados com sucesso!');
      if (window.innerWidth < 960) {
        setMobileView('preview');
      }
    } catch (_) {
      const fallback = buildLocalFallbackPresentation(payload);
      setPresentation(fallback);
      setActiveSlideIdx(0);
      setStatusMessage('Slides prontos!');
      if (window.innerWidth < 960) {
        setMobileView('preview');
      }
    } finally {
      setIsGenerating(false);
    }
  };

  const handleExportPdf = async () => {
    if (!presentation) return;
    setIsExportingPdf(true);
    setStatusMessage('Gerando PDF Full HD...');

    try {
      const exportPayload = {
        ...presentation,
        primaryColor: autoColors ? presentation.primaryColor || effectivePrimary : primaryColor,
        secondaryColor: autoColors ? presentation.secondaryColor || effectiveSecondary : secondaryColor
      };

      const res = await fetch('/api/slides/export-pdf', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(exportPayload)
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      const safeName = (presentation.presentationTitle || 'apresentacao-escolar')
        .toLowerCase()
        .replace(/[^a-z0-9-_]/g, '-')
        .replace(/-+/g, '-');
      a.href = url;
      a.download = `${safeName || 'slides'}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      setStatusMessage('PDF baixado com sucesso!');
    } catch (_) {
      window.print();
      setStatusMessage('Imprimindo ou salvando em PDF...');
    } finally {
      setIsExportingPdf(false);
    }
  };

  useEffect(() => {
    handleGenerateSlides();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const currentSlide =
    presentation && presentation.slides && presentation.slides[activeSlideIdx]
      ? presentation.slides[activeSlideIdx]
      : null;

  const displayPrimary = autoColors
    ? presentation?.primaryColor || effectivePrimary
    : primaryColor;
  const displaySecondary = autoColors
    ? presentation?.secondaryColor || effectiveSecondary
    : secondaryColor;

  return (
    <div className="edu-app">
      {/* Top Header - Compact, clean & mobile friendly */}
      <header className="edu-header no-print">
        <div className="edu-header-left">
          <div className="edu-logo" style={{ backgroundColor: displayPrimary }}>
            <BookOpen size={20} color="#fff" />
          </div>
          <div>
            <h1 className="edu-title">SlideEdu <span>IA</span></h1>
            <p className="edu-sub">Criador de Slides para Estudos e Aulas</p>
          </div>
        </div>

        {/* Mobile View Segmented Switch */}
        <div className="edu-mobile-toggle">
          <button
            type="button"
            className={mobileView === 'edit' ? 'active' : ''}
            onClick={() => setMobileView('edit')}
          >
            <SlidersHorizontal size={15} />
            <span>Editar</span>
          </button>
          <button
            type="button"
            className={mobileView === 'preview' ? 'active' : ''}
            onClick={() => setMobileView('preview')}
          >
            <Eye size={15} />
            <span>Ver Slides</span>
          </button>
        </div>

        <div className="edu-header-actions">
          <button
            type="button"
            className="edu-btn-generate"
            onClick={handleGenerateSlides}
            disabled={isGenerating}
          >
            <Wand2 size={16} />
            <span className="btn-label-desktop">{isGenerating ? 'Criando...' : 'Gerar com IA'}</span>
          </button>

          <button
            type="button"
            className="edu-btn-download"
            onClick={handleExportPdf}
            disabled={isExportingPdf || !presentation}
            style={{ backgroundColor: displayPrimary }}
          >
            <FileDown size={16} />
            <span className="btn-label-desktop">{isExportingPdf ? 'Baixando...' : 'Baixar PDF'}</span>
          </button>
        </div>
      </header>

      {/* Main Content Area */}
      <div className="edu-main-layout no-print">
        {/* Left Control Panel (Edit Mode) */}
        <aside className={`edu-control-panel ${mobileView === 'edit' ? 'show-mobile' : 'hide-mobile'}`}>
          {/* Clean Step Selector */}
          <div className="edu-nav-tabs">
            <button
              type="button"
              className={activeTab === 'ideia' ? 'active' : ''}
              onClick={() => setActiveTab('ideia')}
            >
              <Sparkles size={16} />
              <span>1. Ideia</span>
            </button>
            <button
              type="button"
              className={activeTab === 'paginas' ? 'active' : ''}
              onClick={() => setActiveTab('paginas')}
            >
              <Layers size={16} />
              <span>2. Páginas ({totalPages})</span>
            </button>
            <button
              type="button"
              className={activeTab === 'fotos' ? 'active' : ''}
              onClick={() => setActiveTab('fotos')}
            >
              <ImagePlus size={16} />
              <span>3. Fotos ({uploadedPhotos.length})</span>
            </button>
            <button
              type="button"
              className={activeTab === 'cores' ? 'active' : ''}
              onClick={() => setActiveTab('cores')}
            >
              <Palette size={16} />
              <span>4. Cores</span>
            </button>
          </div>

          <div className="edu-panel-body">
            {/* TAB 1: IDEIA & MATÉRIA */}
            {activeTab === 'ideia' && (
              <div className="edu-tab-content">
                <div className="edu-card">
                  <label className="edu-card-title">Qual é o tema do trabalho ou aula?</label>
                  <textarea
                    rows={3}
                    className="edu-input-textarea"
                    placeholder="Ex: Teorema de Pitágoras e aplicações práticas na construção..."
                    value={idea}
                    onChange={(e) => setIdea(e.target.value)}
                  />
                  <span className="edu-tip">
                    A IA criará títulos didáticos, explicações passo a passo e tópicos claros.
                  </span>
                </div>

                <div className="edu-card">
                  <label className="edu-card-title">Escolha a Matéria:</label>
                  <div className="edu-subject-chips">
                    {SUBJECT_THEMES.map((sub) => {
                      const isSel = subjectTheme === sub.id;
                      return (
                        <button
                          key={sub.id}
                          type="button"
                          className={`edu-chip ${isSel ? 'selected' : ''}`}
                          onClick={() => {
                            setSubjectTheme(sub.id);
                            if (autoColors) {
                              setPrimaryColor(sub.defaultColors[0]);
                              setSecondaryColor(sub.defaultColors[1]);
                            }
                          }}
                        >
                          <span className="edu-chip-icon">{sub.icon}</span>
                          <span className="edu-chip-name">{sub.name}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>

                <button
                  type="button"
                  className="edu-btn-primary"
                  style={{ backgroundColor: displayPrimary }}
                  onClick={() => setActiveTab('paginas')}
                >
                  Continuar: Escolher Páginas e Layouts →
                </button>
              </div>
            )}

            {/* TAB 2: QUANTIDADE DE PÁGINAS & LAYOUTS */}
            {activeTab === 'paginas' && (
              <div className="edu-tab-content">
                <div className="edu-card">
                  <div className="edu-card-header-row">
                    <label className="edu-card-title">Quantas páginas de slides?</label>
                    <span className="edu-badge-pages" style={{ backgroundColor: displayPrimary }}>
                      {totalPages} páginas
                    </span>
                  </div>
                  <div className="edu-page-counter-row">
                    {[2, 3, 4, 5, 6, 8, 10].map((num) => (
                      <button
                        key={num}
                        type="button"
                        className={`edu-num-btn ${totalPages === num ? 'active' : ''}`}
                        onClick={() => handleTotalPagesChange(num)}
                      >
                        {num}
                      </button>
                    ))}
                  </div>
                </div>

                <div className="edu-card">
                  <label className="edu-card-title">Layout de cada página:</label>
                  <p className="edu-tip" style={{ marginBottom: 12 }}>
                    Escolha como quer organizar cada página da apresentação:
                  </p>

                  <div className="edu-page-list">
                    {pageConfigs.map((cfg, idx) => {
                      const pNum = idx + 1;
                      const hasPhoto = uploadedPhotos.some((p) => Number(p.pageNumber) === pNum);
                      return (
                        <div
                          key={pNum}
                          className={`edu-page-row ${activeSlideIdx === idx ? 'highlight' : ''}`}
                          onClick={() => {
                            setActiveSlideIdx(idx);
                            if (window.innerWidth < 960) setMobileView('preview');
                          }}
                        >
                          <div className="edu-page-pos" style={{ backgroundColor: displaySecondary }}>
                            {pNum}
                          </div>

                          <div className="edu-page-preview-box">
                            <LayoutMiniPreview
                              layoutId={cfg.layoutType}
                              primary={displayPrimary}
                              secondary={displaySecondary}
                            />
                          </div>

                          <div className="edu-page-select-area" onClick={(e) => e.stopPropagation()}>
                            <select
                              value={cfg.layoutType}
                              onChange={(e) => updatePageLayout(idx, e.target.value)}
                              className="edu-clean-select"
                            >
                              {PAGE_LAYOUTS.map((l) => (
                                <option key={l.id} value={l.id}>
                                  {l.name}
                                </option>
                              ))}
                            </select>
                            {hasPhoto && <span className="edu-photo-badge">📸 Foto nesta página</span>}
                          </div>

                          <div className="edu-page-order-btns" onClick={(e) => e.stopPropagation()}>
                            <button
                              type="button"
                              disabled={idx === 0}
                              onClick={() => movePagePosition(idx, -1)}
                              title="Subir página"
                            >
                              <ArrowUp size={13} />
                            </button>
                            <button
                              type="button"
                              disabled={idx === pageConfigs.length - 1}
                              onClick={() => movePagePosition(idx, 1)}
                              title="Descer página"
                            >
                              <ArrowDown size={13} />
                            </button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            )}

            {/* TAB 3: UPLOAD DE FOTOS POR PÁGINA */}
            {activeTab === 'fotos' && (
              <div className="edu-tab-content">
                <div className="edu-card">
                  <label className="edu-card-title">Adicionar Fotos ao Slide</label>
                  <label className="edu-upload-box" style={{ borderColor: displayPrimary }}>
                    <ImagePlus size={28} color={displayPrimary} />
                    <strong>Toque para escolher fotos</strong>
                    <span>Envie imagens de gráficos, fórmulas ou fotos pessoais</span>
                    <input
                      type="file"
                      accept="image/*"
                      multiple
                      onChange={handlePhotoUpload}
                      hidden
                    />
                  </label>
                </div>

                {uploadedPhotos.length === 0 ? (
                  <div className="edu-empty-box">
                    Nenhuma foto enviada ainda. As páginas sem foto usarão molduras geométricas limpas automaticamente.
                  </div>
                ) : (
                  <div className="edu-photo-cards">
                    {uploadedPhotos.map((photo) => (
                      <div key={photo.id} className="edu-photo-item">
                        <img src={photo.dataUrl} alt={photo.name} />
                        <div className="edu-photo-controls">
                          <div className="edu-photo-header">
                            <span className="edu-photo-name">{photo.name}</span>
                            <button
                              type="button"
                              className="edu-del-btn"
                              onClick={() => removePhoto(photo.id)}
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>

                          <label>Mostrar em qual página?</label>
                          <select
                            value={photo.pageNumber}
                            onChange={(e) =>
                              updatePhotoAssignment(photo.id, 'pageNumber', e.target.value)
                            }
                            className="edu-clean-select"
                          >
                            {Array.from({ length: totalPages }, (_, i) => i + 1).map((num) => (
                              <option key={num} value={num}>
                                Página {num}
                              </option>
                            ))}
                          </select>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* TAB 4: CORES DO TEMA */}
            {activeTab === 'cores' && (
              <div className="edu-tab-content">
                <div className="edu-card">
                  <label className="edu-card-title">Cores da Apresentação</label>
                  <div className="edu-mode-toggle">
                    <button
                      type="button"
                      className={autoColors ? 'active' : ''}
                      onClick={() => setAutoColors(true)}
                    >
                      <Wand2 size={14} /> Automático por Matéria
                    </button>
                    <button
                      type="button"
                      className={!autoColors ? 'active' : ''}
                      onClick={() => setAutoColors(false)}
                    >
                      <Palette size={14} /> Escolher 2 Cores
                    </button>
                  </div>

                  {!autoColors && (
                    <div className="edu-color-pickers">
                      <div className="edu-color-row">
                        <span>Cor 1 (Destaque):</span>
                        <input
                          type="color"
                          value={primaryColor}
                          onChange={(e) => setPrimaryColor(e.target.value)}
                        />
                        <span className="hex-label">{primaryColor}</span>
                      </div>
                      <div className="edu-color-row">
                        <span>Cor 2 (Base/Texto):</span>
                        <input
                          type="color"
                          value={secondaryColor}
                          onChange={(e) => setSecondaryColor(e.target.value)}
                        />
                        <span className="hex-label">{secondaryColor}</span>
                      </div>
                    </div>
                  )}
                </div>

                <div className="edu-card">
                  <label className="edu-card-title">Paletas Rápidas:</label>
                  <div className="edu-palettes-grid">
                    {COLOR_PALETTES.map((pal, idx) => (
                      <button
                        key={idx}
                        type="button"
                        className="edu-palette-btn"
                        onClick={() => {
                          setAutoColors(false);
                          setPrimaryColor(pal.primary);
                          setSecondaryColor(pal.secondary);
                        }}
                      >
                        <div className="edu-pal-dots">
                          <span style={{ backgroundColor: pal.primary }} />
                          <span style={{ backgroundColor: pal.secondary }} />
                        </div>
                        <span className="edu-pal-name">{pal.name}</span>
                      </button>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </aside>

        {/* Right Preview Stage (Preview Mode) */}
        <main className={`edu-stage ${mobileView === 'preview' ? 'show-mobile' : 'hide-mobile'}`}>
          {/* Top Controls Bar for Slides Navigation */}
          <div className="edu-stage-bar">
            <div className="edu-nav-buttons">
              <button
                type="button"
                className="edu-stage-btn"
                disabled={activeSlideIdx === 0}
                onClick={() => setActiveSlideIdx((i) => Math.max(0, i - 1))}
              >
                <ChevronLeft size={18} /> Anterior
              </button>

              <div className="edu-stage-counter">
                Página <strong>{activeSlideIdx + 1}</strong> de{' '}
                <strong>{presentation?.slides?.length || totalPages}</strong>
              </div>

              <button
                type="button"
                className="edu-stage-btn"
                disabled={
                  !presentation?.slides ||
                  activeSlideIdx >= presentation.slides.length - 1
                }
                onClick={() =>
                  setActiveSlideIdx((i) =>
                    Math.min((presentation?.slides?.length || 1) - 1, i + 1)
                  )
                }
              >
                Próxima <ChevronRight size={18} />
              </button>
            </div>

            <button
              type="button"
              className="edu-fullscreen-btn"
              onClick={() => setFullscreenMode(!fullscreenMode)}
              title="Tela cheia"
            >
              <Maximize2 size={16} />
            </button>
          </div>

          {/* 16:9 Slide Canvas Viewport with responsive scaling */}
          <div className={`edu-viewport ${fullscreenMode ? 'is-fullscreen' : ''}`}>
            {fullscreenMode && (
              <button
                type="button"
                className="edu-exit-fs"
                onClick={() => setFullscreenMode(false)}
              >
                Sair da tela cheia ✕
              </button>
            )}

            <div className="edu-slide-frame">
              {currentSlide && (
                <SlideCanvas16x9
                  slide={currentSlide}
                  pageIndex={activeSlideIdx}
                  totalPages={presentation?.slides?.length || totalPages}
                  primaryColor={displayPrimary}
                  secondaryColor={displaySecondary}
                  presentationTitle={presentation?.presentationTitle}
                />
              )}
            </div>
          </div>

          {/* Bottom Filmstrip Carousel */}
          <div className="edu-filmstrip">
            {(presentation?.slides || []).map((slide, idx) => (
              <div
                key={idx}
                className={`edu-thumb ${activeSlideIdx === idx ? 'active' : ''}`}
                onClick={() => setActiveSlideIdx(idx)}
                style={{
                  borderColor: activeSlideIdx === idx ? displayPrimary : 'transparent'
                }}
              >
                <div className="edu-thumb-header">Pág. {idx + 1}</div>
                <div className="edu-thumb-svg">
                  <LayoutMiniPreview
                    layoutId={slide.layoutType}
                    primary={displayPrimary}
                    secondary={displaySecondary}
                  />
                </div>
              </div>
            ))}
          </div>

          {/* Mobile Bottom Download Bar */}
          <div className="edu-mobile-bottom-bar">
            <button
              type="button"
              className="edu-btn-download-mobile"
              style={{ backgroundColor: displayPrimary }}
              onClick={handleExportPdf}
              disabled={isExportingPdf || !presentation}
            >
              <FileDown size={18} />
              {isExportingPdf ? 'Baixando PDF...' : 'Baixar Slide em PDF'}
            </button>
          </div>
        </main>
      </div>

      {/* Print only container for direct browser PDF generation */}
      <div className="print-only-slides">
        {(presentation?.slides || []).map((s, idx) => (
          <div key={idx} className="print-slide-page">
            <SlideCanvas16x9
              slide={s}
              pageIndex={idx}
              totalPages={presentation?.slides?.length || totalPages}
              primaryColor={displayPrimary}
              secondaryColor={displaySecondary}
              presentationTitle={presentation?.presentationTitle}
            />
          </div>
        ))}
      </div>
    </div>
  );
}

function buildLocalFallbackPresentation(payload) {
  const title = payload.idea
    ? payload.idea.slice(0, 65).toUpperCase()
    : 'APRESENTAÇÃO DIDÁTICA';

  const slides = (payload.pages || []).map((p, i) => ({
    pageNumber: i + 1,
    layoutType: p.layoutType || 'cover-circle',
    badge: `PÁGINA 0${i + 1} • CONCEITO`,
    title:
      p.customTitle ||
      (i === 0
        ? title
        : i === payload.pages.length - 1
        ? 'OBRIGADO PELA ATENÇÃO!'
        : `TÓPICO 0${i}: FUNDAMENTOS`),
    subtitle: `Compreendendo os conceitos essenciais e a lógica de ${payload.idea || 'estudo'}.`,
    bodyText: `Explicação clara e organizada em etapas com exemplos práticos para facilitar o aprendizado e a memorização.`,
    bullets: [
      'Conceito fundamental e definições básicas',
      'Exemplos contextualizados no cotidiano',
      'Resumo dos pontos mais importantes'
    ],
    columns: [
      { number: '01', title: 'CONCEITO BASE', description: 'Definição inicial e regras básicas.' },
      { number: '02', title: 'DESENVOLVIMENTO', description: 'Como aplicar na prática e exercícios.' },
      { number: '03', title: 'APLICAÇÕES', description: 'Exemplos reais e resolução passo a passo.' },
      { number: '04', title: 'CONCLUSÃO', description: 'Síntese final para fixação do conteúdo.' }
    ],
    stats: [
      { value: '95%', label: 'Clareza Didática', description: 'Estruturado para fácil compreensão.' },
      { value: '3x', label: 'Mais Rápido', description: 'Economiza tempo na preparação de aulas.' },
      { value: '100%', label: 'Educacional', description: 'Ideal para Ensino Fundamental, Médio e Faculdade.' }
    ],
    highlightQuote: `“O conhecimento organizado com clareza transforma temas complexos em aprendizado simples.”`,
    photoBase64: p.photoBase64 || null,
    photoCaption: p.photoCaption || null
  }));

  return {
    presentationTitle: title,
    idea: payload.idea,
    subjectTheme: payload.subjectTheme,
    primaryColor: payload.primaryColor,
    secondaryColor: payload.secondaryColor,
    autoColors: payload.autoColors,
    totalPages: slides.length,
    slides
  };
}
