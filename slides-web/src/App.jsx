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
  Edit3,
  History,
  Plus,
  Minus
} from 'lucide-react';
import {
  SUBJECT_THEMES,
  TEMPLATE_STYLES,
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
  'closing-thankyou',
  'split-photo-right',
  'timeline-steps',
  'quote-vision',
  'cover-diagonal'
];

export default function App() {
  // 1. Idea, Subject & Template Model
  const [idea, setIdea] = useState(
    'Teorema de Pitágoras, relações trigonométricas no triângulo retângulo e aplicações práticas na arquitetura'
  );
  const [subjectTheme, setSubjectTheme] = useState('matematica');
  const [templateStyle, setTemplateStyle] = useState('geometric-modern');

  // 2. Colors (2 colors or Automatic AI)
  const [autoColors, setAutoColors] = useState(true);
  const [primaryColor, setPrimaryColor] = useState('#F97316');
  const [secondaryColor, setSecondaryColor] = useState('#1E293B');

  // 3. Total Pages & Per-Page Layout Configuration
  const [totalPages, setTotalPages] = useState(6);
  const [pageConfigs, setPageConfigs] = useState(() =>
    Array.from({ length: 6 }, (_, i) => ({
      pageNumber: i + 1,
      layoutType: i === 0 ? 'cover-circle' : i === 5 ? 'closing-thankyou' : DEFAULT_LAYOUT_ORDER[i % DEFAULT_LAYOUT_ORDER.length],
      customTitle: '',
      customNotes: ''
    }))
  );

  // 4. Uploaded Photos mapped to Page Numbers
  const [uploadedPhotos, setUploadedPhotos] = useState([]);

  // 5. Generated Presentation & Active Preview State
  const [presentation, setPresentation] = useState(null);
  const [activeSlideIdx, setActiveSlideIdx] = useState(0);
  const [isGenerating, setIsGenerating] = useState(false);
  const [isExportingPdf, setIsExportingPdf] = useState(false);
  const [activeTab, setActiveTab] = useState('ideia'); // 'ideia' | 'paginas' | 'fotos' | 'cores' | 'editor'
  const [fullscreenMode, setFullscreenMode] = useState(false);
  const [historyList, setHistoryList] = useState([]);
  const [statusMessage, setStatusMessage] = useState('');

  // Keep pageConfigs synchronized when totalPages changes
  const handleTotalPagesChange = (newTotal) => {
    const clamped = Math.max(2, Math.min(15, Number(newTotal) || 6));
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
            customTitle: '',
            customNotes: ''
          });
        }
      }
      return next;
    });
  };

  // Update layout or notes of a specific page position
  const updatePageConfig = (index, field, value) => {
    setPageConfigs((prev) => {
      const copy = [...prev];
      copy[index] = { ...copy[index], [field]: value };
      return copy;
    });

    // Also update live presentation slide if already generated
    if (presentation && presentation.slides && presentation.slides[index]) {
      setPresentation((prev) => {
        const slidesCopy = [...prev.slides];
        slidesCopy[index] = {
          ...slidesCopy[index],
          ...(field === 'layoutType' ? { layoutType: value } : {}),
          ...(field === 'customTitle' && value ? { title: value } : {})
        };
        return { ...prev, slides: slidesCopy };
      });
    }
  };

  // Move a page position up or down
  const movePagePosition = (index, direction) => {
    const targetIdx = index + direction;
    if (targetIdx < 0 || targetIdx >= pageConfigs.length) return;

    const oldPageA = index + 1;
    const oldPageB = targetIdx + 1;

    setPageConfigs((prev) => {
      const copy = [...prev];
      const temp = copy[index];
      copy[index] = { ...copy[targetIdx], pageNumber: oldPageA };
      copy[targetIdx] = { ...temp, pageNumber: oldPageB };
      return copy;
    });

    // Also swap photo page assignments if photos were attached to those pages
    setUploadedPhotos((prev) =>
      prev.map((p) => {
        if (p.pageNumber === oldPageA) return { ...p, pageNumber: oldPageB };
        if (p.pageNumber === oldPageB) return { ...p, pageNumber: oldPageA };
        return p;
      })
    );
  };

  // Handle Multi-Photo Upload
  const handlePhotoUpload = (e) => {
    const files = Array.from(e.target.files || []);
    if (!files.length) return;

    files.forEach((file, idx) => {
      const reader = new FileReader();
      reader.onload = () => {
        const base64 = reader.result;
        setUploadedPhotos((prev) => {
          // Pick first page that doesn't have a photo yet, or page 1
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

  // Resolve effective 2 colors for preview
  const getEffectiveColors = () => {
    if (!autoColors) {
      return [primaryColor, secondaryColor];
    }
    const foundTheme = SUBJECT_THEMES.find((t) => t.id === subjectTheme);
    return foundTheme ? foundTheme.defaultColors : ['#F97316', '#1E293B'];
  };

  const [effectivePrimary, effectiveSecondary] = getEffectiveColors();

  // Generate Presentation via Spring Boot Backend (with local fallback if offline)
  const handleGenerateSlides = async () => {
    setIsGenerating(true);
    setStatusMessage('Gerando estrutura visual, conteúdo inteligente e layouts 16:9...');

    const pagesPayload = pageConfigs.map((cfg, idx) => {
      const pageNum = idx + 1;
      const assignedPhoto = uploadedPhotos.find((p) => Number(p.pageNumber) === pageNum);
      return {
        pageNumber: pageNum,
        layoutType: cfg.layoutType,
        customTitle: cfg.customTitle || null,
        customNotes: cfg.customNotes || null,
        photoBase64: assignedPhoto ? assignedPhoto.dataUrl : null,
        photoCaption: assignedPhoto ? assignedPhoto.caption : null
      };
    });

    const payload = {
      idea,
      subjectTheme,
      templateStyle,
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

      if (!res.ok) {
        throw new Error(`HTTP ${res.status}`);
      }
      const data = await res.json();
      setPresentation(data);
      setActiveSlideIdx(0);
      setStatusMessage('Apresentação gerada com sucesso! Você pode editar qualquer texto ou baixar o PDF.');
      fetchHistory();
    } catch (err) {
      // Fallback local generation if backend is starting
      const fallback = buildLocalFallbackPresentation(payload);
      setPresentation(fallback);
      setActiveSlideIdx(0);
      setStatusMessage('Apresentação pronta no visualizador 16:9!');
    } finally {
      setIsGenerating(false);
    }
  };

  // Export PDF from Spring Boot Backend (/api/slides/export-pdf)
  const handleExportPdf = async () => {
    if (!presentation) return;
    setIsExportingPdf(true);
    setStatusMessage('Renderizando páginas em alta definição Full HD (16:9) e gerando arquivo PDF...');

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

      if (!res.ok) {
        throw new Error(`Erro HTTP ${res.status}`);
      }

      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      const safeName = (presentation.presentationTitle || 'apresentacao-slides')
        .toLowerCase()
        .replace(/[^a-z0-9-_]/g, '-')
        .replace(/-+/g, '-');
      a.href = url;
      a.download = `${safeName || 'slides'}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      setStatusMessage('Download do PDF concluído com sucesso!');
    } catch (err) {
      // Print-to-PDF fallback
      window.print();
      setStatusMessage('PDF aberto para salvamento!');
    } finally {
      setIsExportingPdf(false);
    }
  };

  const fetchHistory = async () => {
    try {
      const res = await fetch('/api/slides/history');
      if (res.ok) {
        const list = await res.json();
        setHistoryList(list);
      }
    } catch (_) {}
  };

  useEffect(() => {
    handleGenerateSlides();
    fetchHistory();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Select Subject Theme & load sample idea if desired
  const handleSelectSubject = (themeObj) => {
    setSubjectTheme(themeObj.id);
    if (autoColors) {
      setPrimaryColor(themeObj.defaultColors[0]);
      setSecondaryColor(themeObj.defaultColors[1]);
    }
  };

  // Edit active slide content inline
  const updateActiveSlideField = (field, value) => {
    if (!presentation) return;
    setPresentation((prev) => {
      const slidesCopy = [...prev.slides];
      slidesCopy[activeSlideIdx] = {
        ...slidesCopy[activeSlideIdx],
        [field]: value
      };
      return { ...prev, slides: slidesCopy };
    });
  };

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
    <div className="app-shell">
      {/* Top Navbar */}
      <header className="top-navbar no-print">
        <div className="brand-left">
          <div className="brand-logo" style={{ background: `linear-gradient(135deg, ${displayPrimary}, ${displaySecondary})` }}>
            <Layers size={22} color="#fff" />
          </div>
          <div>
            <div className="brand-title">
              SlideCraft <span>AI Studio</span>
            </div>
            <div className="brand-sub">
              Criador de Apresentações 16:9 com Layouts Por Posição & Exportador PDF
            </div>
          </div>
        </div>

        <div className="top-actions">
          <div className="color-pill-indicator">
            <span className="cpi-dot" style={{ backgroundColor: displayPrimary }} />
            <span className="cpi-dot" style={{ backgroundColor: displaySecondary }} />
            <span className="cpi-text">
              {autoColors ? 'Cores IA Automáticas' : '2 Cores Personalizadas'}
            </span>
          </div>

          <button
            type="button"
            className="btn-generate-top"
            onClick={handleGenerateSlides}
            disabled={isGenerating}
          >
            <Wand2 size={18} />
            {isGenerating ? 'Gerando Slides...' : 'Gerar / Atualizar Slides'}
          </button>

          <button
            type="button"
            className="btn-export-pdf"
            onClick={handleExportPdf}
            disabled={isExportingPdf || !presentation}
            style={{ backgroundColor: displayPrimary }}
          >
            <FileDown size={18} />
            {isExportingPdf ? 'Exportando PDF...' : 'Baixar Slide em PDF'}
          </button>
        </div>
      </header>

      {/* Main Studio Workspace */}
      <div className="studio-workspace no-print">
        {/* Left Configuration Sidebar */}
        <aside className="config-sidebar">
          <div className="step-tabs">
            <button
              type="button"
              className={`step-tab ${activeTab === 'ideia' ? 'active' : ''}`}
              onClick={() => setActiveTab('ideia')}
            >
              <BookOpen size={16} />
              <span>1. Ideia & Tema</span>
            </button>
            <button
              type="button"
              className={`step-tab ${activeTab === 'paginas' ? 'active' : ''}`}
              onClick={() => setActiveTab('paginas')}
            >
              <Layers size={16} />
              <span>2. Páginas ({totalPages})</span>
            </button>
            <button
              type="button"
              className={`step-tab ${activeTab === 'fotos' ? 'active' : ''}`}
              onClick={() => setActiveTab('fotos')}
            >
              <ImagePlus size={16} />
              <span>3. Fotos ({uploadedPhotos.length})</span>
            </button>
            <button
              type="button"
              className={`step-tab ${activeTab === 'cores' ? 'active' : ''}`}
              onClick={() => setActiveTab('cores')}
            >
              <Palette size={16} />
              <span>4. Cores</span>
            </button>
            <button
              type="button"
              className={`step-tab ${activeTab === 'editor' ? 'active' : ''}`}
              onClick={() => setActiveTab('editor')}
            >
              <Edit3 size={16} />
              <span>5. Editar</span>
            </button>
          </div>

          <div className="sidebar-scroll-content">
            {/* TAB 1: IDEIA, DISCIPLINA & MODELO */}
            {activeTab === 'ideia' && (
              <div className="tab-pane">
                <div className="section-block">
                  <label className="section-label">
                    <Sparkles size={16} /> Digite a Ideia ou Tema do seu Slide
                  </label>
                  <textarea
                    rows={4}
                    className="idea-textarea"
                    placeholder="Ex: Explique as Três Leis de Newton com fórmulas, exemplos do cotidiano e exercícios..."
                    value={idea}
                    onChange={(e) => setIdea(e.target.value)}
                  />
                  <div className="helper-note">
                    A IA estrutura os títulos, tópicos, colunas 01–04, estatísticas e síntese com base na sua ideia.
                  </div>
                </div>

                <div className="section-block">
                  <label className="section-label">
                    <BookOpen size={16} /> Escolha a Matéria / Tema do Slide
                  </label>
                  <div className="subject-grid">
                    {SUBJECT_THEMES.map((subj) => {
                      const selected = subjectTheme === subj.id;
                      return (
                        <div
                          key={subj.id}
                          className={`subject-card ${selected ? 'selected' : ''}`}
                          onClick={() => handleSelectSubject(subj)}
                        >
                          <div className="sc-top">
                            <span className="sc-icon">{subj.icon}</span>
                            <div className="sc-color-dots">
                              <span style={{ backgroundColor: subj.defaultColors[0] }} />
                              <span style={{ backgroundColor: subj.defaultColors[1] }} />
                            </div>
                          </div>
                          <div className="sc-name">{subj.name}</div>
                          <div className="sc-desc">{subj.description}</div>
                          <button
                            type="button"
                            className="sc-use-sample"
                            onClick={(ev) => {
                              ev.stopPropagation();
                              handleSelectSubject(subj);
                              setIdea(subj.sampleIdea);
                            }}
                          >
                            Usar exemplo de ideia
                          </button>
                        </div>
                      );
                    })}
                  </div>
                </div>

                <div className="section-block">
                  <label className="section-label">
                    <Sliders size={16} /> Estilo de Modelo Visual
                  </label>
                  <div className="template-style-list">
                    {TEMPLATE_STYLES.map((st) => (
                      <div
                        key={st.id}
                        className={`template-style-item ${templateStyle === st.id ? 'selected' : ''}`}
                        onClick={() => setTemplateStyle(st.id)}
                      >
                        <div className="tsi-header">
                          <strong>{st.name}</strong>
                          <span className="tsi-badge">{st.badge}</span>
                        </div>
                        <p>{st.description}</p>
                      </div>
                    ))}
                  </div>
                </div>

                <button
                  type="button"
                  className="btn-primary-full"
                  style={{ backgroundColor: displayPrimary }}
                  onClick={() => setActiveTab('paginas')}
                >
                  Próximo: Definir Páginas e Layouts →
                </button>
              </div>
            )}

            {/* TAB 2: QUANTIDADE DE PÁGINAS & LAYOUT POR POSIÇÃO */}
            {activeTab === 'paginas' && (
              <div className="tab-pane">
                <div className="section-block">
                  <label className="section-label">
                    <Layers size={16} /> Quantas páginas terá o seu Slide?
                  </label>
                  <div className="page-count-control">
                    <button
                      type="button"
                      className="pcc-btn"
                      onClick={() => handleTotalPagesChange(totalPages - 1)}
                    >
                      <Minus size={16} />
                    </button>
                    <input
                      type="range"
                      min={2}
                      max={15}
                      value={totalPages}
                      onChange={(e) => handleTotalPagesChange(e.target.value)}
                    />
                    <button
                      type="button"
                      className="pcc-btn"
                      onClick={() => handleTotalPagesChange(totalPages + 1)}
                    >
                      <Plus size={16} />
                    </button>
                    <span className="pcc-badge" style={{ backgroundColor: displayPrimary }}>
                      {totalPages} Páginas
                    </span>
                  </div>
                </div>

                <div className="section-block">
                  <label className="section-label">
                    Posição das Páginas & Escolha de Layout por Página
                  </label>
                  <p className="section-subtext">
                    Selecione qual modelo de layout vai ficar em cada posição (Página 1, Página 2...) e use as setas para mudar a ordem:
                  </p>

                  <div className="page-sequence-list">
                    {pageConfigs.map((cfg, idx) => {
                      const pageNum = idx + 1;
                      const assignedPhoto = uploadedPhotos.find((p) => Number(p.pageNumber) === pageNum);
                      const currentLayoutMeta =
                        PAGE_LAYOUTS.find((l) => l.id === cfg.layoutType) || PAGE_LAYOUTS[0];

                      return (
                        <div
                          key={pageNum}
                          className={`page-seq-card ${activeSlideIdx === idx ? 'active-page-card' : ''}`}
                          onClick={() => setActiveSlideIdx(idx)}
                        >
                          <div className="psc-top">
                            <div className="psc-pos-badge" style={{ backgroundColor: displaySecondary }}>
                              Página {pageNum}
                            </div>
                            <span className="psc-category">{currentLayoutMeta.category}</span>

                            <div className="psc-order-btns" onClick={(e) => e.stopPropagation()}>
                              <button
                                type="button"
                                title="Subir posição da página"
                                disabled={idx === 0}
                                onClick={() => movePagePosition(idx, -1)}
                              >
                                <ArrowUp size={14} />
                              </button>
                              <button
                                type="button"
                                title="Descer posição da página"
                                disabled={idx === pageConfigs.length - 1}
                                onClick={() => movePagePosition(idx, 1)}
                              >
                                <ArrowDown size={14} />
                              </button>
                            </div>
                          </div>

                          <div className="psc-body" onClick={(e) => e.stopPropagation()}>
                            <div className="psc-mini-thumb">
                              <LayoutMiniPreview
                                layoutId={cfg.layoutType}
                                primary={displayPrimary}
                                secondary={displaySecondary}
                              />
                            </div>

                            <div className="psc-controls">
                              <label>Layout da Página {pageNum}:</label>
                              <select
                                value={cfg.layoutType}
                                onChange={(e) => updatePageConfig(idx, 'layoutType', e.target.value)}
                              >
                                {PAGE_LAYOUTS.map((l) => (
                                  <option key={l.id} value={l.id}>
                                    {l.name} ({l.category})
                                  </option>
                                ))}
                              </select>

                              <input
                                type="text"
                                placeholder={`Título opcional para a Página ${pageNum}...`}
                                value={cfg.customTitle}
                                onChange={(e) => updatePageConfig(idx, 'customTitle', e.target.value)}
                              />
                            </div>
                          </div>

                          <div className="psc-footer">
                            <span>{currentLayoutMeta.description}</span>
                            {assignedPhoto && (
                              <span className="psc-photo-tag">
                                📸 Foto vinculada ({assignedPhoto.name})
                              </span>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>

                {/* Quick Gallery of all 10 Layouts to assign to current selected page */}
                <div className="section-block">
                  <label className="section-label">
                    Galeria Visual de Layouts (Clique para aplicar na Página {activeSlideIdx + 1})
                  </label>
                  <div className="layout-gallery-grid">
                    {PAGE_LAYOUTS.map((layout) => {
                      const isCurrent =
                        pageConfigs[activeSlideIdx]?.layoutType === layout.id;
                      return (
                        <div
                          key={layout.id}
                          className={`layout-gallery-item ${isCurrent ? 'selected' : ''}`}
                          onClick={() =>
                            updatePageConfig(activeSlideIdx, 'layoutType', layout.id)
                          }
                        >
                          <LayoutMiniPreview
                            layoutId={layout.id}
                            primary={displayPrimary}
                            secondary={displaySecondary}
                          />
                          <div className="lgi-title">{layout.name}</div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              </div>
            )}

            {/* TAB 3: UPLOAD DE FOTOS E SELEÇÃO DE PÁGINA */}
            {activeTab === 'fotos' && (
              <div className="tab-pane">
                <div className="section-block">
                  <label className="section-label">
                    <ImagePlus size={16} /> Upload de Fotos por Página
                  </label>
                  <p className="section-subtext">
                    Envie suas fotos e selecione exatamente em qual página (Página 1 a Página {totalPages}) cada foto será exibida:
                  </p>

                  <label className="photo-dropzone" style={{ borderColor: displayPrimary }}>
                    <ImagePlus size={32} color={displayPrimary} />
                    <strong>Clique para fazer upload de fotos</strong>
                    <span>PNG, JPG ou WEBP — Você pode selecionar várias fotos de uma vez</span>
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
                  <div className="empty-photos-box">
                    Nenhuma foto enviada ainda. Os slides sem foto usarão ilustrações geométricas modernas nas cores do seu tema automaticamente.
                  </div>
                ) : (
                  <div className="uploaded-photos-list">
                    {uploadedPhotos.map((photo) => (
                      <div key={photo.id} className="uploaded-photo-card">
                        <img src={photo.dataUrl} alt={photo.name} className="upc-thumb" />
                        <div className="upc-info">
                          <div className="upc-top-row">
                            <strong className="upc-name">{photo.name}</strong>
                            <button
                              type="button"
                              className="upc-del-btn"
                              onClick={() => removePhoto(photo.id)}
                              title="Remover foto"
                            >
                              <Trash2 size={15} />
                            </button>
                          </div>

                          <label className="upc-label">
                            Exibir esta foto em qual página?
                          </label>
                          <select
                            value={photo.pageNumber}
                            onChange={(e) =>
                              updatePhotoAssignment(photo.id, 'pageNumber', e.target.value)
                            }
                            className="upc-page-select"
                          >
                            {Array.from({ length: totalPages }, (_, i) => i + 1).map((pNum) => (
                              <option key={pNum} value={pNum}>
                                Página {pNum} — ({
                                  PAGE_LAYOUTS.find(
                                    (l) => l.id === pageConfigs[pNum - 1]?.layoutType
                                  )?.name || 'Slide'
                                })
                              </option>
                            ))}
                          </select>

                          <input
                            type="text"
                            placeholder="Legenda opcional da foto..."
                            value={photo.caption}
                            onChange={(e) =>
                              updatePhotoAssignment(photo.id, 'caption', e.target.value)
                            }
                            className="upc-caption-input"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* TAB 4: ESCOLHA DE 2 CORES OU IA AUTOMÁTICA */}
            {activeTab === 'cores' && (
              <div className="tab-pane">
                <div className="section-block">
                  <label className="section-label">
                    <Palette size={16} /> Cores do Tema (2 Cores ou IA Automática)
                  </label>

                  <div className="color-mode-switch">
                    <button
                      type="button"
                      className={`cms-btn ${autoColors ? 'active' : ''}`}
                      onClick={() => setAutoColors(true)}
                    >
                      <Wand2 size={16} />
                      IA escolhe automaticamente
                    </button>
                    <button
                      type="button"
                      className={`cms-btn ${!autoColors ? 'active' : ''}`}
                      onClick={() => setAutoColors(false)}
                    >
                      <Palette size={16} />
                      Escolher minhas 2 cores
                    </button>
                  </div>

                  {autoColors ? (
                    <div className="ai-color-notice">
                      <CheckCircle2 size={18} color="#10b981" />
                      <div>
                        <strong>Modo Automático com IA Ativado</strong>
                        <p>
                          A IA analisa o tema ({SUBJECT_THEMES.find((t) => t.id === subjectTheme)?.name}) e aplica automaticamente a combinação ideal de 2 cores contrastantes.
                        </p>
                      </div>
                    </div>
                  ) : (
                    <div className="manual-color-pickers">
                      <div className="mcp-box">
                        <label>1ª Cor (Principal / Destaque)</label>
                        <div className="mcp-input-row">
                          <input
                            type="color"
                            value={primaryColor}
                            onChange={(e) => setPrimaryColor(e.target.value)}
                          />
                          <input
                            type="text"
                            value={primaryColor}
                            onChange={(e) => setPrimaryColor(e.target.value)}
                          />
                        </div>
                      </div>

                      <div className="mcp-box">
                        <label>2ª Cor (Secundária / Contraste)</label>
                        <div className="mcp-input-row">
                          <input
                            type="color"
                            value={secondaryColor}
                            onChange={(e) => setSecondaryColor(e.target.value)}
                          />
                          <input
                            type="text"
                            value={secondaryColor}
                            onChange={(e) => setSecondaryColor(e.target.value)}
                          />
                        </div>
                      </div>
                    </div>
                  )}
                </div>

                <div className="section-block">
                  <label className="section-label">
                    Paletas de 2 Cores Recomendadas (Clique para testar)
                  </label>
                  <div className="palettes-grid">
                    {COLOR_PALETTES.map((pal, i) => (
                      <div
                        key={i}
                        className="palette-item"
                        onClick={() => {
                          setAutoColors(false);
                          setPrimaryColor(pal.primary);
                          setSecondaryColor(pal.secondary);
                        }}
                      >
                        <div className="pi-swatches">
                          <span style={{ backgroundColor: pal.primary }} />
                          <span style={{ backgroundColor: pal.secondary }} />
                        </div>
                        <span className="pi-name">{pal.name}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}

            {/* TAB 5: EDITOR RÁPIDO DA PÁGINA ATUAL & HISTÓRICO */}
            {activeTab === 'editor' && currentSlide && (
              <div className="tab-pane">
                <div className="section-block">
                  <label className="section-label">
                    <Edit3 size={16} /> Editando Página {activeSlideIdx + 1} de {presentation?.slides?.length || totalPages}
                  </label>

                  <div className="editor-field">
                    <label>Layout desta página:</label>
                    <select
                      value={currentSlide.layoutType}
                      onChange={(e) => {
                        updateActiveSlideField('layoutType', e.target.value);
                        updatePageConfig(activeSlideIdx, 'layoutType', e.target.value);
                      }}
                    >
                      {PAGE_LAYOUTS.map((l) => (
                        <option key={l.id} value={l.id}>
                          {l.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="editor-field">
                    <label>Selo Superior (Badge):</label>
                    <input
                      type="text"
                      value={currentSlide.badge || ''}
                      onChange={(e) => updateActiveSlideField('badge', e.target.value)}
                    />
                  </div>

                  <div className="editor-field">
                    <label>Título da Página:</label>
                    <input
                      type="text"
                      value={currentSlide.title || ''}
                      onChange={(e) => updateActiveSlideField('title', e.target.value)}
                    />
                  </div>

                  <div className="editor-field">
                    <label>Subtítulo:</label>
                    <input
                      type="text"
                      value={currentSlide.subtitle || ''}
                      onChange={(e) => updateActiveSlideField('subtitle', e.target.value)}
                    />
                  </div>

                  <div className="editor-field">
                    <label>Texto Explicativo:</label>
                    <textarea
                      rows={3}
                      value={currentSlide.bodyText || ''}
                      onChange={(e) => updateActiveSlideField('bodyText', e.target.value)}
                    />
                  </div>
                </div>

                {historyList.length > 0 && (
                  <div className="section-block">
                    <label className="section-label">
                      <History size={16} /> Últimas Apresentações Salvas no Banco
                    </label>
                    <div className="history-items">
                      {historyList.slice(0, 6).map((item) => (
                        <div
                          key={item.id}
                          className="history-card"
                          onClick={() => {
                            if (item.slides && item.slides.length > 0) {
                              setPresentation(item);
                              setActiveSlideIdx(0);
                            }
                          }}
                        >
                          <div className="hc-dots">
                            <span style={{ backgroundColor: item.primaryColor }} />
                            <span style={{ backgroundColor: item.secondaryColor }} />
                          </div>
                          <div className="hc-meta">
                            <strong>{item.presentationTitle}</strong>
                            <span>{item.totalPages} págs • {item.createdAt}</span>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="sidebar-bottom-bar">
            <button
              type="button"
              className="btn-generate-main"
              onClick={handleGenerateSlides}
              disabled={isGenerating}
              style={{ backgroundColor: displaySecondary }}
            >
              <Sparkles size={18} />
              {isGenerating ? 'Gerando...' : 'Gerar Slide com IA'}
            </button>
          </div>
        </aside>

        {/* Right Live Preview Stage (16:9 Widescreen) */}
        <main className="preview-stage">
          {/* Stage Top Bar */}
          <div className="stage-toolbar">
            <div className="st-left">
              <span className="st-pres-title">
                {presentation?.presentationTitle || 'Preview da Apresentação'}
              </span>
              {statusMessage && <span className="st-status-pill">{statusMessage}</span>}
            </div>

            <div className="st-controls">
              <button
                type="button"
                className="st-nav-btn"
                disabled={activeSlideIdx === 0}
                onClick={() => setActiveSlideIdx((i) => Math.max(0, i - 1))}
              >
                <ChevronLeft size={18} /> Anterior
              </button>
              <span className="st-page-counter">
                Página <strong>{activeSlideIdx + 1}</strong> de{' '}
                <strong>{presentation?.slides?.length || totalPages}</strong>
              </span>
              <button
                type="button"
                className="st-nav-btn"
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

              <button
                type="button"
                className="st-icon-btn"
                title="Tela Cheia"
                onClick={() => setFullscreenMode(!fullscreenMode)}
              >
                <Maximize2 size={17} />
              </button>
            </div>
          </div>

          {/* 16:9 Widescreen Canvas Viewport */}
          <div className={`canvas-viewport ${fullscreenMode ? 'fullscreen-canvas' : ''}`}>
            {fullscreenMode && (
              <button
                type="button"
                className="exit-fs-btn"
                onClick={() => setFullscreenMode(false)}
              >
                Sair da Tela Cheia (ESC)
              </button>
            )}

            <div className="aspect-16x9-frame">
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

          {/* Bottom Filmstrip of All Pages */}
          <div className="filmstrip-bar">
            {(presentation?.slides || []).map((s, idx) => {
              const hasPhoto = Boolean(s.photoBase64);
              return (
                <div
                  key={idx}
                  className={`filmstrip-thumb ${activeSlideIdx === idx ? 'active' : ''}`}
                  onClick={() => setActiveSlideIdx(idx)}
                  style={{
                    borderColor: activeSlideIdx === idx ? displayPrimary : 'transparent'
                  }}
                >
                  <div className="ft-top">
                    <span className="ft-num">Pág. {idx + 1}</span>
                    {hasPhoto && <span className="ft-cam">📸</span>}
                  </div>
                  <div className="ft-mini-preview">
                    <LayoutMiniPreview
                      layoutId={s.layoutType}
                      primary={displayPrimary}
                      secondary={displaySecondary}
                    />
                  </div>
                  <div className="ft-title">{s.title}</div>
                </div>
              );
            })}
          </div>
        </main>
      </div>

      {/* Print-only all slides container for fallback PDF print */}
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

// Local immediate fallback builder if API is not yet reachable
function buildLocalFallbackPresentation(payload) {
  const title = payload.idea
    ? payload.idea.slice(0, 62).toUpperCase()
    : 'APRESENTAÇÃO EXECUTIVA & EDUCACIONAL';
  const badge = (payload.subjectTheme || 'SLIDES').toUpperCase();

  const slides = (payload.pages || []).map((p, i) => ({
    pageNumber: i + 1,
    layoutType: p.layoutType || 'cover-circle',
    badge: `PÁGINA 0${i + 1} • ${badge}`,
    title:
      p.customTitle ||
      (i === 0
        ? title
        : i === payload.pages.length - 1
        ? 'OBRIGADO PELA ATENÇÃO!'
        : `TÓPICO ESTRUTURAL 0${i}`),
    subtitle:
      p.customNotes ||
      `Análise didática e aplicação prática sobre ${payload.idea || 'o tema principal'}.`,
    bodyText: `Desenvolvimento conceitual estruturado para a página ${i + 1}, combinando clareza visual, síntese objetiva e exemplos contextualizados.`,
    bullets: [
      'Fundamentação teórica e conceitos essenciais',
      'Relações práticas e demonstração passo a passo',
      'Síntese de resultados e aplicação direta'
    ],
    columns: [
      { number: '01', title: 'FUNDAMENTO BASE', description: 'Princípios centrais e definições iniciais.' },
      { number: '02', title: 'ESTRUTURA LÓGICA', description: 'Regras, variáveis e conexões principais.' },
      { number: '03', title: 'CASOS PRÁTICOS', description: 'Aplicação real em exercícios e projetos.' },
      { number: '04', title: 'CONCLUSÃO CHAVE', description: 'Resultados e consolidação do aprendizado.' }
    ],
    stats: [
      { value: '92%', label: 'Precisão Conceitual', description: 'Domínio completo dos fundamentos.' },
      { value: '43%', label: 'Ganho de Eficiência', description: 'Metodologia estruturada em etapas.' },
      { value: '100%', label: 'Aplicabilidade', description: 'Conexão direta com problemas reais.' }
    ],
    highlightQuote: `“Compreender ${payload.idea || 'este tema'} com método visual claro transforma teoria em domínio prático.”`,
    photoBase64: p.photoBase64 || null,
    photoCaption: p.photoCaption || null
  }));

  return {
    presentationTitle: title,
    idea: payload.idea,
    subjectTheme: payload.subjectTheme,
    templateStyle: payload.templateStyle,
    primaryColor: payload.primaryColor,
    secondaryColor: payload.secondaryColor,
    autoColors: payload.autoColors,
    totalPages: slides.length,
    slides
  };
}

