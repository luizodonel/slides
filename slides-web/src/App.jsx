import React, { useState, useEffect } from 'react';
import {
  Sparkles,
  FileDown,
  Layers,
  Palette,
  Camera,
  Trash2,
  ArrowUp,
  ArrowDown,
  ChevronLeft,
  ChevronRight,
  Wand2,
  BookOpen,
  Maximize2,
  Eye,
  SlidersHorizontal,
  Upload,
  CreditCard,
  Lock,
  CheckCircle2,
  Edit3,
  Plus,
  MessageCircle,
  FileText
} from 'lucide-react';
import {
  SUBJECT_THEMES,
  COLOR_PALETTES,
  PAGE_LAYOUTS
} from './data/slideTemplates';
import LayoutMiniPreview from './components/LayoutMiniPreview';
import SlideCanvas16x9 from './components/SlideCanvas16x9';
import PaymentModal from './components/PaymentModal';
import PdfPreviewModal from './components/PdfPreviewModal';
import VisualLayoutSelectorModal from './components/VisualLayoutSelectorModal';

const DEFAULT_LAYOUT_ORDER = [
  'cover-circle',
  'split-photo-left',
  'columns-4',
  'infographic-stats',
  'grid-4-cards',
  'closing-thankyou'
];

export default function App() {
  // Mobile/Tablet View: 'edit' | 'preview' (padrão 'edit' em celulares e tablets)
  const [mobileView, setMobileView] = useState('edit');

  // 1. Idea & Subject
  const [idea, setIdea] = useState(
    'Teorema de Pitágoras e relações no triângulo retângulo aplicadas na arquitetura'
  );
  const [subjectTheme, setSubjectTheme] = useState('matematica');

  // 2. Colors (Auto AI vs Manual 2 Colors)
  const [autoColors, setAutoColors] = useState(true);
  const [primaryColor, setPrimaryColor] = useState('#2563EB');
  const [secondaryColor, setSecondaryColor] = useState('#0F172A');

  // 3. Pages & Layouts with Direct Photo on each page
  const [totalPages, setTotalPages] = useState(4);
  const [pageConfigs, setPageConfigs] = useState(() =>
    Array.from({ length: 4 }, (_, i) => ({
      pageNumber: i + 1,
      layoutType: i === 0 ? 'cover-circle' : i === 3 ? 'closing-thankyou' : DEFAULT_LAYOUT_ORDER[i % DEFAULT_LAYOUT_ORDER.length],
      customTitle: '',
      photoBase64: null,
      photoName: null,
      photoCaption: ''
    }))
  );

  // Active Presentation State
  const [presentation, setPresentation] = useState(null);
  const [activeSlideIdx, setActiveSlideIdx] = useState(0);
  const [isGenerating, setIsGenerating] = useState(false);
  const [isExportingPdf, setIsExportingPdf] = useState(false);
  const [activeTab, setActiveTab] = useState('ideia'); // 'ideia' | 'paginas' | 'cores'
  const [fullscreenMode, setFullscreenMode] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');

  // Payment State (Mercado Pago R$ 1,50 / slide)
  const [isPaid, setIsPaid] = useState(false);
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
  const [paidPaymentId, setPaidPaymentId] = useState(null);
  const [isPreviewPdfOpen, setIsPreviewPdfOpen] = useState(false);

  // Visual Layout Selector Modal
  const [isLayoutModalOpen, setIsLayoutModalOpen] = useState(false);
  const [layoutModalPageIdx, setLayoutModalPageIdx] = useState(0);

  // Texto do Trabalho para Geração Automática com IA
  const [assignmentText, setAssignmentText] = useState('');
  const MIN_ASSIGNMENT_CHARS = 80;

  const calculateEstimatedPages = (length) => {
    if (length < 260) return 2;
    if (length < 600) return 3;
    if (length < 1100) return 4;
    return 5;
  };

  // Handle Total Pages Change
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
            customTitle: '',
            photoBase64: null,
            photoName: null,
            photoCaption: ''
          });
        }
      }
      return next;
    });

    // Sincroniza os slides da apresentação ativa em tempo real
    if (presentation && presentation.slides) {
      setPresentation((prev) => {
        let slides = [...(prev.slides || [])];
        if (slides.length > clamped) {
          slides = slides.slice(0, clamped);
        } else if (slides.length < clamped) {
          const title = prev.presentationTitle || idea || 'APRESENTAÇÃO DIDÁTICA';
          for (let i = slides.length; i < clamped; i++) {
            const isLast = i === clamped - 1;
            slides.push({
              pageNumber: i + 1,
              layoutType: isLast ? 'closing-thankyou' : DEFAULT_LAYOUT_ORDER[i % DEFAULT_LAYOUT_ORDER.length],
              badge: `PÁGINA 0${i + 1} • CONCEITO`,
              title: isLast ? 'OBRIGADO PELA ATENÇÃO!' : `TÓPICO 0${i + 1}: DESENVOLVIMENTO`,
              subtitle: `Compreendendo os conceitos essenciais e a lógica de ${idea || 'estudo'}.`,
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
              photoBase64: null,
              photoCaption: null
            });
          }
        }
        slides = slides.map((s, idx) => ({ ...s, pageNumber: idx + 1 }));
        return {
          ...prev,
          totalPages: clamped,
          slides
        };
      });
    }

    setActiveSlideIdx((curr) => Math.min(curr, clamped - 1));
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

  // Direct Photo Upload for a specific page
  const handlePagePhotoUpload = (index, file) => {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      const base64 = reader.result;
      setPageConfigs((prev) => {
        const copy = [...prev];
        copy[index] = {
          ...copy[index],
          photoBase64: base64,
          photoName: file.name
        };
        return copy;
      });

      if (presentation && presentation.slides && presentation.slides[index]) {
        setPresentation((prev) => {
          const slidesCopy = [...prev.slides];
          slidesCopy[index] = {
            ...slidesCopy[index],
            photoBase64: base64
          };
          return { ...prev, slides: slidesCopy };
        });
      }
    };
    reader.readAsDataURL(file);
  };

  const removePagePhoto = (index) => {
    setPageConfigs((prev) => {
      const copy = [...prev];
      copy[index] = {
        ...copy[index],
        photoBase64: null,
        photoName: null,
        photoCaption: ''
      };
      return copy;
    });

    if (presentation && presentation.slides && presentation.slides[index]) {
      setPresentation((prev) => {
        const slidesCopy = [...prev.slides];
        slidesCopy[index] = {
          ...slidesCopy[index],
          photoBase64: null,
          photoCaption: null
        };
        return { ...prev, slides: slidesCopy };
      });
    }
  };

  const movePagePosition = (index, direction) => {
    const targetIdx = index + direction;
    if (targetIdx < 0 || targetIdx >= pageConfigs.length) return;

    setPageConfigs((prev) => {
      const copy = [...prev];
      const temp = copy[index];
      copy[index] = { ...copy[targetIdx], pageNumber: index + 1 };
      copy[targetIdx] = { ...temp, pageNumber: targetIdx + 1 };
      return copy;
    });

    if (presentation && presentation.slides) {
      setPresentation((prev) => {
        const slidesCopy = [...prev.slides];
        const temp = slidesCopy[index];
        slidesCopy[index] = { ...slidesCopy[targetIdx], pageNumber: index + 1 };
        slidesCopy[targetIdx] = { ...temp, pageNumber: targetIdx + 1 };
        return { ...prev, slides: slidesCopy };
      });
    }
  };

  // Atualização em Tempo Real da Ideia/Tema
  const handleIdeaChange = (newIdea) => {
    setIdea(newIdea);
    if (presentation && presentation.slides) {
      setPresentation((prev) => {
        const slides = [...prev.slides];
        if (slides.length > 0) {
          slides[0] = {
            ...slides[0],
            title: newIdea ? newIdea.toUpperCase() : 'SEU TEMA AQUI',
            subtitle: newIdea
              ? `Compreendendo os conceitos essenciais e a lógica de ${newIdea}.`
              : slides[0].subtitle
          };
        }
        return {
          ...prev,
          presentationTitle: newIdea || 'Apresentação',
          slides
        };
      });
    }
  };

  // Edição em Tempo Real dos Campos do Slide Ativo
  const updateCurrentSlideField = (field, value) => {
    if (!presentation || !presentation.slides) return;
    setPresentation((prev) => {
      const slides = [...prev.slides];
      slides[activeSlideIdx] = {
        ...slides[activeSlideIdx],
        [field]: value
      };
      return { ...prev, slides };
    });
  };

  const updateCurrentSlideBullet = (bIdx, value) => {
    if (!presentation || !presentation.slides) return;
    setPresentation((prev) => {
      const slides = [...prev.slides];
      const current = slides[activeSlideIdx];
      const bullets = [...(current.bullets || [])];
      bullets[bIdx] = value;
      slides[activeSlideIdx] = { ...current, bullets };
      return { ...prev, slides };
    });
  };

  const addCurrentSlideBullet = () => {
    if (!presentation || !presentation.slides) return;
    setPresentation((prev) => {
      const slides = [...prev.slides];
      const current = slides[activeSlideIdx];
      const bullets = [...(current.bullets || []), 'Novo ponto ou conceito relevante'];
      slides[activeSlideIdx] = { ...current, bullets };
      return { ...prev, slides };
    });
  };

  const removeCurrentSlideBullet = (bIdx) => {
    if (!presentation || !presentation.slides) return;
    setPresentation((prev) => {
      const slides = [...prev.slides];
      const current = slides[activeSlideIdx];
      const bullets = (current.bullets || []).filter((_, i) => i !== bIdx);
      slides[activeSlideIdx] = { ...current, bullets };
      return { ...prev, slides };
    });
  };

  const ensureColumns = (slide) => {
    if (slide && slide.columns && slide.columns.length > 0) return slide.columns;
    return [
      { number: '01', title: 'CONCEITO BASE', description: 'Definição inicial e regras básicas.' },
      { number: '02', title: 'DESENVOLVIMENTO', description: 'Como aplicar na prática e exercícios.' },
      { number: '03', title: 'APLICAÇÕES', description: 'Exemplos reais e resolução passo a passo.' },
      { number: '04', title: 'CONCLUSÃO', description: 'Síntese final para fixação do conteúdo.' }
    ];
  };

  const updateCurrentSlideColumn = (colIdx, field, value) => {
    if (!presentation || !presentation.slides) return;
    setPresentation((prev) => {
      const slides = [...prev.slides];
      const current = slides[activeSlideIdx];
      const baseCols = ensureColumns(current);
      const columns = baseCols.map((c) => ({ ...c }));
      if (columns[colIdx]) {
        columns[colIdx] = { ...columns[colIdx], [field]: value };
      }
      slides[activeSlideIdx] = { ...current, columns };
      return { ...prev, slides };
    });
  };

  const ensureStats = (slide) => {
    if (slide && slide.stats && slide.stats.length > 0) return slide.stats;
    return [
      { value: '95%', label: 'Clareza Didática', description: 'Estruturado para fácil compreensão.' },
      { value: '3x', label: 'Mais Rápido', description: 'Economiza tempo na preparação de aulas.' },
      { value: '100%', label: 'Educacional', description: 'Ideal para Ensino Fundamental, Médio e Faculdade.' }
    ];
  };

  const updateCurrentSlideStat = (statIdx, field, value) => {
    if (!presentation || !presentation.slides) return;
    setPresentation((prev) => {
      const slides = [...prev.slides];
      const current = slides[activeSlideIdx];
      const baseStats = ensureStats(current);
      const stats = baseStats.map((s) => ({ ...s }));
      if (stats[statIdx]) {
        stats[statIdx] = { ...stats[statIdx], [field]: value };
      }
      slides[activeSlideIdx] = { ...current, stats };
      return { ...prev, slides };
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

    const pagesPayload = pageConfigs.map((cfg, idx) => ({
      pageNumber: idx + 1,
      layoutType: cfg.layoutType,
      customTitle: cfg.customTitle || null,
      photoBase64: cfg.photoBase64 || null,
      photoCaption: cfg.photoCaption || null
    }));

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
        setMobileView('edit');
      }
    } catch (_) {
      const fallback = buildLocalFallbackPresentation(payload);
      setPresentation(fallback);
      setActiveSlideIdx(0);
      setStatusMessage('Slides prontos!');
      if (window.innerWidth < 960) {
        setMobileView('edit');
      }
    } finally {
      setIsGenerating(false);
    }
  };

  // Geração Automática com IA a partir do Texto do Trabalho
  const handleCreateFromAssignmentText = async () => {
    const raw = assignmentText.trim();
    if (raw.length < MIN_ASSIGNMENT_CHARS) return;

    setIsGenerating(true);
    setStatusMessage('Analisando seu trabalho e montando os slides com IA...');

    // 1. Extração Inteligente do Texto
    const lines = raw.split(/\r?\n/).map((l) => l.trim()).filter(Boolean);
    const sentences = raw
      .replace(/([.?!])\s+/g, '$1|')
      .split('|')
      .map((s) => s.trim().replace(/\s+/g, ' '))
      .filter((s) => s.length > 8);

    // Título Principal
    let detectedTitle = '';
    if (lines.length > 0 && lines[0].length <= 80 && !lines[0].endsWith('.')) {
      detectedTitle = lines[0];
    } else if (sentences.length > 0) {
      detectedTitle = sentences[0].slice(0, 65).replace(/[.?!]+$/, '');
    } else {
      detectedTitle = 'APRESENTAÇÃO DIDÁTICA';
    }

    // Disciplina e Cores
    const lower = raw.toLowerCase();
    let detectedSubject = 'negocios';
    let pCol = '#2563EB';
    let sCol = '#0F172A';

    if (/matem|equa|geometr|pitág|cálcul|fórmula|trigono/i.test(lower)) {
      detectedSubject = 'matematica';
      pCol = '#2563EB';
      sCol = '#0F172A';
    } else if (/biolog|célul|ecolog|genét|dna|fotossínt|organism|corpo/i.test(lower)) {
      detectedSubject = 'biologia';
      pCol = '#10B981';
      sCol = '#064E3B';
    } else if (/físic|newton|energia|óptic|termodin|gravidad|velocidad/i.test(lower)) {
      detectedSubject = 'fisica';
      pCol = '#7C3AED';
      sCol = '#1E1B4B';
    } else if (/químic|átomo|reação|molécul|tabela periód|ácid/i.test(lower)) {
      detectedSubject = 'quimica';
      pCol = '#06B6D4';
      sCol = '#0F172A';
    } else if (/histór|guerra|sécul|impér|revoluç|brasil impér|idade médi/i.test(lower)) {
      detectedSubject = 'historia';
      pCol = '#B45309';
      sCol = '#1C1917';
    } else if (/geograf|clima|relev|relevo|geopolít|continente|populaç/i.test(lower)) {
      detectedSubject = 'geografia';
      pCol = '#0284C7';
      sCol = '#0C4A6E';
    } else if (/portugu|gramát|literat|redação|poesia|modernism|verbo/i.test(lower)) {
      detectedSubject = 'portugues';
      pCol = '#D97706';
      sCol = '#27272A';
    } else if (/tecnolog|software|inteligência|ia|computad|sistema|program/i.test(lower)) {
      detectedSubject = 'tecnologia';
      pCol = '#6366F1';
      sCol = '#090D16';
    }

    // Quantidade de páginas calculada automaticamente (mínimo de 2)
    const targetPages = calculateEstimatedPages(raw.length);
    setIdea(detectedTitle);
    setSubjectTheme(detectedSubject);
    setPrimaryColor(pCol);
    setSecondaryColor(sCol);
    setTotalPages(targetPages);

    // Sequência ideal de layouts
    const layoutSequence = [];
    if (targetPages === 2) {
      layoutSequence.push('cover-circle', 'split-photo-left');
    } else if (targetPages === 3) {
      layoutSequence.push('cover-circle', 'split-photo-left', 'closing-thankyou');
    } else if (targetPages === 4) {
      const hasStats = /\b\d+([.,]\d+)?\s*(%|x|milhões|mil|vezes)\b/i.test(raw);
      layoutSequence.push(
        'cover-circle',
        'split-photo-left',
        hasStats ? 'infographic-stats' : 'grid-4-cards',
        'closing-thankyou'
      );
    } else {
      layoutSequence.push(
        'cover-circle',
        'split-photo-left',
        'grid-4-cards',
        'infographic-stats',
        'closing-thankyou'
      );
    }

    // Configurações das páginas
    const newConfigs = layoutSequence.map((layout, i) => ({
      pageNumber: i + 1,
      layoutType: layout,
      customTitle: '',
      photoBase64: null,
      photoName: null,
      photoCaption: ''
    }));
    setPageConfigs(newConfigs);

    // Tópicos extraídos das frases
    const bulletsPool = sentences.length > 2
      ? sentences.slice(1, 10).map((s) => s.slice(0, 95))
      : [
          'Compreensão dos fundamentos e objetivos centrais',
          'Aplicações práticas e análise dos resultados',
          'Síntese conclusiva e pontos de fixação'
        ];

    const generatedSlides = layoutSequence.map((layout, idx) => {
      const pageNum = idx + 1;
      const isFirst = idx === 0;
      const isLast = idx === targetPages - 1;

      const slideBullets = [
        bulletsPool[idx % bulletsPool.length] || 'Fundamento essencial do estudo',
        bulletsPool[(idx + 1) % bulletsPool.length] || 'Aplicação prática e mecanismos',
        bulletsPool[(idx + 2) % bulletsPool.length] || 'Evidências e fixação de aprendizado'
      ];

      return {
        pageNumber: pageNum,
        layoutType: layout,
        badge: `PÁGINA 0${pageNum} • CONCEITO`,
        title: isFirst
          ? detectedTitle.toUpperCase()
          : isLast
          ? 'OBRIGADO PELA ATENÇÃO!'
          : `TÓPICO 0${pageNum}: DESENVOLVIMENTO`,
        subtitle: isFirst
          ? (sentences[1] ? sentences[1].slice(0, 140) : `Compreendendo os conceitos essenciais e a lógica de ${detectedTitle}.`)
          : `Análise estruturada e detalhamento sobre ${detectedTitle}.`,
        bodyText: sentences[idx] ? sentences[idx].slice(0, 260) : raw.slice(0, 200),
        bullets: slideBullets,
        columns: [
          { number: '01', title: 'CONCEITO BASE', description: bulletsPool[0] || 'Definição inicial e regras básicas.' },
          { number: '02', title: 'DESENVOLVIMENTO', description: bulletsPool[1] || 'Como aplicar na prática e exercícios.' },
          { number: '03', title: 'APLICAÇÕES', description: bulletsPool[2] || 'Exemplos reais e resolução passo a passo.' },
          { number: '04', title: 'CONCLUSÃO', description: bulletsPool[3] || 'Síntese final para fixação do conteúdo.' }
        ],
        stats: [
          { value: '01', label: bulletsPool[0] ? bulletsPool[0].slice(0, 30).toUpperCase() : 'CONCEITO BASE', description: bulletsPool[0] || 'Fundamentos e princípios do conteúdo.' },
          { value: '02', label: bulletsPool[1] ? bulletsPool[1].slice(0, 30).toUpperCase() : 'DESENVOLVIMENTO', description: bulletsPool[1] || 'Aprofundamento e aplicações práticas.' },
          { value: '03', label: bulletsPool[2] ? bulletsPool[2].slice(0, 30).toUpperCase() : 'SÍNTESE & RESULTADOS', description: bulletsPool[2] || 'Conclusões e impacto dos conceitos estudados.' }
        ],
        highlightQuote: `“${sentences[0] ? sentences[0].slice(0, 120) : 'O conhecimento organizado transforma temas complexos em aprendizado simples.'}”`,
        photoBase64: null,
        photoCaption: null
      };
    });

    const newPresentation = {
      presentationTitle: detectedTitle,
      idea: detectedTitle,
      subjectTheme: detectedSubject,
      primaryColor: pCol,
      secondaryColor: sCol,
      autoColors: true,
      totalPages: targetPages,
      slides: generatedSlides
    };

    setPresentation(newPresentation);
    setActiveSlideIdx(0);
    setIsGenerating(false);
    setStatusMessage(`🎉 Slides criados com sucesso! Geramos ${targetPages} páginas a partir do seu trabalho.`);
    setActiveTab('paginas');
  };

  const handleExportPdf = async (customPaymentId = null) => {
    if (!presentation) return;
    const effectivePaymentId = customPaymentId || paidPaymentId;

    if (!isPaid && !effectivePaymentId) {
      setIsPaymentModalOpen(true);
      return;
    }

    setIsExportingPdf(true);
    setStatusMessage('Gerando PDF Oficial Full HD...');

    try {
      const exportPayload = {
        ...presentation,
        primaryColor: autoColors ? presentation.primaryColor || effectivePrimary : primaryColor,
        secondaryColor: autoColors ? presentation.secondaryColor || effectiveSecondary : secondaryColor
      };

      const url = `/api/slides/export-pdf?paymentId=${encodeURIComponent(effectivePaymentId || '')}`;
      const res = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(exportPayload)
      });

      if (!res.ok) throw new Error(`HTTP ${res.status}`);

      const blob = await res.blob();
      const blobUrl = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      const safeName = (presentation.presentationTitle || 'slides-da-oninha')
        .toLowerCase()
        .replace(/[^a-z0-9-_]/g, '-')
        .replace(/-+/g, '-');
      a.href = blobUrl;
      a.download = `${safeName || 'slides'}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(blobUrl);
      setStatusMessage('PDF oficial baixado com sucesso!');
    } catch (_) {
      window.print();
      setStatusMessage('Imprimindo apresentação...');
    } finally {
      setIsExportingPdf(false);
    }
  };

  const handlePaymentSuccess = (paymentId) => {
    setIsPaid(true);
    setPaidPaymentId(paymentId);
    setStatusMessage('🎉 Pagamento aprovado! Liberando PDF oficial...');
    handleExportPdf(paymentId);
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
      {/* Header */}
      <header className="edu-header no-print">
        <div className="edu-header-left">
          <div className="edu-logo-wrapper">
            <img
              src="/logo-oninha.jpg"
              alt="Logo Oninha"
              className="edu-logo-img"
            />
          </div>
          <div>
            <h1 className="edu-title">Slides da Oninha</h1>
            <p className="edu-sub">Criador de Slides para Estudos e Aulas</p>
          </div>
        </div>

        {/* Mobile View Toggle */}
        <div className="edu-mobile-toggle">
          <button
            type="button"
            className={`edu-toggle-btn edu-toggle-edit ${mobileView === 'edit' ? 'active' : ''}`}
            onClick={() => setMobileView('edit')}
          >
            <SlidersHorizontal size={15} />
            <span>Editar</span>
          </button>
          <button
            type="button"
            className={`edu-toggle-btn edu-toggle-preview ${mobileView === 'preview' ? 'active' : ''}`}
            onClick={() => setMobileView('preview')}
          >
            <Eye size={15} />
            <span>Ver Slides</span>
          </button>
        </div>

        <div className="edu-header-actions">
          <a
            href="https://wa.me/5561985823912?text=Ol%C3%A1!%20Preciso%20de%20ajuda%20com%20meus%20slides%20no%20Slides%20da%20Oninha."
            target="_blank"
            rel="noopener noreferrer"
            className="edu-btn-whatsapp-header"
            title="Suporte por WhatsApp (61) 98582-3912"
          >
            <MessageCircle size={15} />
            <span className="btn-label-desktop">Suporte WhatsApp</span>
          </a>

          <button
            type="button"
            className="edu-btn-preview-pdf"
            onClick={() => setIsPreviewPdfOpen(true)}
            disabled={!presentation}
            title="Visualizar como vai ficar o documento PDF"
          >
            <FileText size={16} />
            <span className="btn-label-desktop">Visualizar PDF</span>
          </button>

          {!isPaid ? (
            <button
              type="button"
              className="edu-btn-download edu-btn-payment-trigger"
              onClick={() => setIsPaymentModalOpen(true)}
              style={{ backgroundColor: '#10b981' }}
              title="Liberar sem marca d'água"
            >
              <CreditCard size={16} />
              <span className="btn-label-desktop">
                Liberar Slides (R$ {(totalPages * 1.5).toFixed(2).replace('.', ',')})
              </span>
            </button>
          ) : (
            <button
              type="button"
              className="edu-btn-download edu-btn-paid-download"
              onClick={() => handleExportPdf()}
              disabled={isExportingPdf || !presentation}
              style={{ backgroundColor: displayPrimary }}
              title="Baixar PDF Oficial sem rascunho"
            >
              <FileDown size={16} />
              <span className="btn-label-desktop">{isExportingPdf ? 'Baixando...' : 'Baixar PDF Oficial'}</span>
            </button>
          )}
        </div>
      </header>

      {/* Main Layout */}
      <div className="edu-main-layout no-print">
        {/* Left Control Panel */}
        <aside className={`edu-control-panel ${mobileView === 'edit' ? 'show-mobile' : 'hide-mobile'}`}>
          {/* 4 Clean Tabs: 1. Ideia | 2. Páginas | 3. Editar Textos | 4. Cores */}
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
              <span>2. Páginas & Fotos ({totalPages})</span>
            </button>
            <button
              type="button"
              className={activeTab === 'editar' ? 'active' : ''}
              onClick={() => setActiveTab('editar')}
            >
              <Edit3 size={16} />
              <span>3. Editar Textos (Pág. {activeSlideIdx + 1})</span>
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
                {/* 1. Criar Slides com o Texto do seu Trabalho (IA Automática) */}
                <div className="edu-card edu-generate-from-work-card">
                  <div className="edu-card-header-row" style={{ marginBottom: 6 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <Sparkles size={16} color="#38bdf8" />
                      <label className="edu-card-title">Criar Slides com o Texto do seu Trabalho</label>
                    </div>
                    <span className="edu-badge-ai">IA Automática</span>
                  </div>
                  <p className="edu-tip" style={{ marginBottom: 10, lineHeight: 1.4 }}>
                    Cole o conteúdo ou resumo do seu trabalho. A IA analisa o texto, define a quantidade ideal de páginas (mínimo de 2 páginas) e escolhe os melhores modelos automaticamente.
                  </p>

                  <textarea
                    rows={4}
                    className="edu-assignment-textarea"
                    placeholder="Cole aqui o texto do seu trabalho ou aula... (mínimo de 80 caracteres para gerar os slides)"
                    value={assignmentText}
                    onChange={(e) => setAssignmentText(e.target.value)}
                  />

                  <div className="edu-char-counter-row">
                    {assignmentText.trim().length < MIN_ASSIGNMENT_CHARS ? (
                      <span className="edu-char-counter is-short">
                        🔴 {assignmentText.trim().length} / {MIN_ASSIGNMENT_CHARS} caracteres (digite mais {MIN_ASSIGNMENT_CHARS - assignmentText.trim().length} para liberar)
                      </span>
                    ) : (
                      <span className="edu-char-counter is-ready">
                        🟢 {assignmentText.trim().length} caracteres • Pronto para gerar (~{calculateEstimatedPages(assignmentText.trim().length)} páginas)
                      </span>
                    )}
                  </div>

                  <button
                    type="button"
                    className="edu-btn-create-from-text"
                    disabled={assignmentText.trim().length < MIN_ASSIGNMENT_CHARS || isGenerating}
                    onClick={handleCreateFromAssignmentText}
                  >
                    <Sparkles size={16} />
                    <span>{isGenerating ? 'Criando Slides com IA...' : '✨ Criar Slides Automaticamente'}</span>
                  </button>
                </div>

                {/* 2. Deseja um slide personalizado? */}
                <div className="edu-card edu-custom-slide-card">
                  <label className="edu-card-title">Deseja um slide personalizado?</label>
                  <p className="edu-tip" style={{ marginBottom: 10 }}>
                    Montamos uma apresentação exclusiva para o seu trabalho ou aula. Fale com a gente!
                  </p>
                  <a
                    href="https://wa.me/5561985823912?text=Ol%C3%A1!%20Gostaria%20de%20um%20slide%20personalizado%20no%20Slides%20da%20Oninha."
                    target="_blank"
                    rel="noopener noreferrer"
                    className="edu-btn-open-whatsapp"
                  >
                    <MessageCircle size={16} />
                    <span>Chamar no WhatsApp</span>
                  </a>
                </div>

                <button
                  type="button"
                  className="edu-btn-primary"
                  style={{ backgroundColor: displayPrimary }}
                  onClick={() => setActiveTab('paginas')}
                >
                  Continuar: Escolher Páginas e Fotos →
                </button>
              </div>
            )}

            {/* TAB 2: PÁGINAS, LAYOUTS & UPLOAD DE FOTOS DIRETO NA PÁGINA */}
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
                    Escolha o modelo e adicione fotos diretamente na página desejada:
                  </p>

                  <div className="edu-page-list">
                    {pageConfigs.map((cfg, idx) => {
                      const pNum = idx + 1;
                      const hasPhoto = Boolean(cfg.photoBase64);
                      const fileInputId = `page-photo-input-${idx}`;

                      return (
                        <div
                          key={pNum}
                          className={`edu-page-row ${activeSlideIdx === idx ? 'highlight' : ''}`}
                          onClick={() => setActiveSlideIdx(idx)}
                        >
                          <div className="edu-page-pos" style={{ backgroundColor: displaySecondary }}>
                            {pNum}
                          </div>

                          <div
                            className="edu-page-preview-box"
                            onClick={(e) => {
                              e.stopPropagation();
                              setActiveSlideIdx(idx);
                              setLayoutModalPageIdx(idx);
                              setIsLayoutModalOpen(true);
                            }}
                            title="Toque para ver e trocar o modelo visual deste slide"
                          >
                            <LayoutMiniPreview
                              layoutId={cfg.layoutType}
                              primary={displayPrimary}
                              secondary={displaySecondary}
                            />
                            <span className="edu-preview-tap-hint">Trocar Modelo</span>
                          </div>

                          <div className="edu-page-select-area" onClick={(e) => e.stopPropagation()}>
                            <button
                              type="button"
                              className="edu-btn-change-layout"
                              onClick={() => {
                                setActiveSlideIdx(idx);
                                setLayoutModalPageIdx(idx);
                                setIsLayoutModalOpen(true);
                              }}
                              title="Toque para ver todos os modelos de slides com imagens"
                            >
                              <span className="edu-layout-name-display">
                                {PAGE_LAYOUTS.find((l) => l.id === cfg.layoutType)?.name || cfg.layoutType}
                              </span>
                              <span className="edu-layout-picker-pill">Ver Modelos ▾</span>
                            </button>

                            {/* Direct Photo Upload Control on this page */}
                            <div className="edu-page-photo-bar" onClick={(e) => e.stopPropagation()}>
                              {hasPhoto ? (
                                <div className="edu-photo-attached">
                                  <img
                                    src={cfg.photoBase64}
                                    alt="Foto"
                                    className="edu-mini-attached-thumb"
                                  />
                                  <span className="edu-attached-name">Foto adicionada</span>
                                  <button
                                    type="button"
                                    className="edu-action-link"
                                    style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0 }}
                                    title="Trocar foto"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      const inputEl = document.getElementById(fileInputId);
                                      if (inputEl) inputEl.click();
                                    }}
                                  >
                                    Trocar
                                  </button>
                                  <button
                                    type="button"
                                    className="edu-photo-trash"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      removePagePhoto(idx);
                                    }}
                                    title="Remover foto"
                                  >
                                    <Trash2 size={13} />
                                  </button>
                                </div>
                              ) : (
                                <button
                                  type="button"
                                  className="edu-btn-add-photo"
                                  title="Adicionar foto a esta página"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    const inputEl = document.getElementById(fileInputId);
                                    if (inputEl) inputEl.click();
                                  }}
                                >
                                  <Camera size={13} />
                                  <span>+ Foto nesta pág</span>
                                </button>
                              )}

                              <input
                                id={fileInputId}
                                type="file"
                                accept="image/*"
                                style={{ display: 'none' }}
                                onChange={(e) => {
                                  if (e.target.files && e.target.files[0]) {
                                    handlePagePhotoUpload(idx, e.target.files[0]);
                                  }
                                  e.target.value = '';
                                }}
                              />

                              <button
                                type="button"
                                className="edu-btn-edit-page-content"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setActiveSlideIdx(idx);
                                  setActiveTab('editar');
                                }}
                                title="Editar títulos, tópicos e cartões desta página"
                              >
                                <Edit3 size={12} />
                                <span>Editar Textos</span>
                              </button>
                            </div>
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

            {/* TAB 3: EDITAR TEXTOS DA PÁGINA ATIVA EM TEMPO REAL */}
            {activeTab === 'editar' && (
              <div className="edu-tab-content">
                <div className="edu-card">
                  <div className="edu-card-header-row">
                    <div>
                      <label className="edu-card-title">
                        Editar Textos da Página {activeSlideIdx + 1}
                      </label>
                      <span className="edu-tip" style={{ display: 'block', marginTop: 2 }}>
                        Altere os campos abaixo para ver o slide atualizar na mesma hora:
                      </span>
                    </div>
                  </div>

                  {/* Seletor Rápido de Página */}
                  <div className="edu-edit-slide-selector">
                    <button
                      type="button"
                      className="edu-num-btn"
                      disabled={activeSlideIdx === 0}
                      onClick={() => setActiveSlideIdx((i) => Math.max(0, i - 1))}
                    >
                      <ChevronLeft size={14} /> Pág. Anterior
                    </button>
                    <span className="edu-edit-slide-badge">
                      Página <strong>{activeSlideIdx + 1}</strong> de{' '}
                      <strong>{presentation?.slides?.length || totalPages}</strong>
                    </span>
                    <button
                      type="button"
                      className="edu-num-btn"
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
                      Próxima Pág. <ChevronRight size={14} />
                    </button>
                  </div>
                </div>

                {currentSlide && (() => {
                  const layoutType = currentSlide.layoutType || 'split-photo-left';

                  const hasSubtitle = [
                    'cover-circle',
                    'cover-diagonal',
                    'split-photo-left',
                    'columns-4',
                    'grid-4-cards',
                    'timeline-steps',
                    'closing-thankyou'
                  ].includes(layoutType);

                  const hasBodyText = [
                    'split-photo-left',
                    'split-photo-right',
                    'infographic-stats',
                    'closing-thankyou'
                  ].includes(layoutType);

                  const hasBullets = [
                    'cover-circle',
                    'cover-diagonal',
                    'split-photo-left',
                    'split-photo-right',
                    'closing-thankyou'
                  ].includes(layoutType);

                  const hasColumns = [
                    'columns-4',
                    'grid-4-cards',
                    'timeline-steps'
                  ].includes(layoutType);

                  const hasStats = layoutType === 'infographic-stats';
                  const isQuoteVision = layoutType === 'quote-vision';
                  const layoutInfo = PAGE_LAYOUTS.find((l) => l.id === layoutType);

                  return (
                    <div className="edu-card edu-edit-fields-container">
                      <div style={{ marginBottom: 4 }}>
                        <span className="edu-tip" style={{ fontSize: '0.78rem', color: '#94a3b8' }}>
                          Modelo desta página: <strong style={{ color: '#38bdf8' }}>{layoutInfo ? layoutInfo.name : layoutType}</strong>
                        </span>
                      </div>

                      {/* Título Principal do Slide (todos os modelos possuem título) */}
                      <div className="edu-edit-field-group">
                        <label className="edu-edit-label">Título da Página {activeSlideIdx + 1}:</label>
                        <input
                          type="text"
                          className="edu-edit-input"
                          value={currentSlide.title || ''}
                          onChange={(e) => updateCurrentSlideField('title', e.target.value)}
                          placeholder="Ex: TEOREMA DE PITÁGORAS E RELAÇÕES"
                        />
                      </div>

                      {/* Subtítulo / Introdução (somente modelos que exibem subtítulo) */}
                      {hasSubtitle && (
                        <div className="edu-edit-field-group">
                          <label className="edu-edit-label">Subtítulo / Introdução:</label>
                          <textarea
                            rows={2}
                            className="edu-edit-input"
                            value={currentSlide.subtitle || ''}
                            onChange={(e) => updateCurrentSlideField('subtitle', e.target.value)}
                            placeholder="Ex: Compreendendo os conceitos essenciais e a lógica..."
                          />
                        </div>
                      )}

                      {/* Texto Corrido (somente modelos que possuem parágrafo explicativo) */}
                      {hasBodyText && (
                        <div className="edu-edit-field-group">
                          <label className="edu-edit-label">Texto / Explicação do Conteúdo:</label>
                          <textarea
                            rows={3}
                            className="edu-edit-input"
                            value={currentSlide.bodyText || ''}
                            onChange={(e) => updateCurrentSlideField('bodyText', e.target.value)}
                            placeholder="Explicação didática detalhada..."
                          />
                        </div>
                      )}

                      {/* CARTÕES E TÓPICOS 01 - 04 (SOMENTE em modelos com grade/colunas/etapas!) */}
                      {hasColumns && (
                        <div className="edu-edit-field-group">
                          <label className="edu-edit-label" style={{ color: '#38bdf8' }}>
                            Cartões e Tópicos 01 - 04 (Grade e Colunas):
                          </label>
                          {ensureColumns(currentSlide).slice(0, 4).map((col, cIdx) => (
                            <div key={cIdx} className="edu-column-edit-card">
                              <div className="edu-column-edit-header">
                                <span className="edu-column-num-badge">0{cIdx + 1}</span>
                                <input
                                  type="text"
                                  className="edu-edit-input"
                                  style={{ fontWeight: 'bold' }}
                                  value={col.title || ''}
                                  onChange={(e) =>
                                    updateCurrentSlideColumn(cIdx, 'title', e.target.value)
                                  }
                                  placeholder={`Título do Cartão 0${cIdx + 1} (Ex: CONCEITO BASE)`}
                                />
                              </div>
                              <textarea
                                rows={2}
                                className="edu-edit-input"
                                style={{ marginTop: 4 }}
                                value={col.description || ''}
                                onChange={(e) =>
                                  updateCurrentSlideColumn(cIdx, 'description', e.target.value)
                                }
                                placeholder={`Descrição do Cartão 0${cIdx + 1} (Ex: Definição inicial e regras básicas.)`}
                              />
                            </div>
                          ))}
                        </div>
                      )}

                      {/* 3 PILARES EM DESTAQUE COM CÍRCULOS */}
                      {hasStats && (
                        <div className="edu-edit-field-group">
                          <label className="edu-edit-label" style={{ color: '#38bdf8' }}>
                            3 Pilares em Destaque (Círculo, Título e Descrição):
                          </label>
                          {ensureStats(currentSlide).slice(0, 3).map((st, sIdx) => (
                            <div key={sIdx} className="edu-column-edit-card">
                              <div className="edu-column-edit-header" style={{ gap: 8, display: 'flex' }}>
                                <input
                                  type="text"
                                  className="edu-edit-input"
                                  style={{ width: '85px', fontWeight: 'bold', color: '#38bdf8', textAlign: 'center' }}
                                  value={st.value || ''}
                                  onChange={(e) => updateCurrentSlideStat(sIdx, 'value', e.target.value)}
                                  placeholder={`0${sIdx + 1}`}
                                />
                                <input
                                  type="text"
                                  className="edu-edit-input"
                                  style={{ flex: 1, fontWeight: 'bold' }}
                                  value={st.label || ''}
                                  onChange={(e) => updateCurrentSlideStat(sIdx, 'label', e.target.value)}
                                  placeholder={`Título do Pilar 0${sIdx + 1}`}
                                />
                              </div>
                              <textarea
                                rows={2}
                                className="edu-edit-input"
                                style={{ marginTop: 4 }}
                                value={st.description || ''}
                                onChange={(e) => updateCurrentSlideStat(sIdx, 'description', e.target.value)}
                                placeholder={`Descrição do Pilar 0${sIdx + 1}`}
                              />
                            </div>
                          ))}
                        </div>
                      )}

                      {/* TÓPICOS EM MARCADORES (SOMENTE em modelos com lista de pontos/bullets!) */}
                      {hasBullets && (
                        <div className="edu-edit-field-group">
                          <label className="edu-edit-label">Pontos em Tópicos (Lista):</label>
                          {(currentSlide.bullets || []).slice(0, 3).map((bullet, bIdx) => (
                            <div key={bIdx} className="edu-bullet-edit-row">
                              <span className="edu-bullet-num">▸ {bIdx + 1}</span>
                              <input
                                type="text"
                                className="edu-edit-input"
                                style={{ flex: 1 }}
                                value={bullet}
                                onChange={(e) => updateCurrentSlideBullet(bIdx, e.target.value)}
                                placeholder={`Ponto ${bIdx + 1}...`}
                              />
                            </div>
                          ))}
                        </div>
                      )}

                      {/* CITAÇÃO / FRASE & CAIXAS (SOMENTE se layout quote-vision) */}
                      {isQuoteVision && (
                        <>
                          <div className="edu-edit-field-group">
                            <label className="edu-edit-label">Frase de Destaque / Citação:</label>
                            <textarea
                              rows={3}
                              className="edu-edit-input"
                              value={currentSlide.highlightQuote || currentSlide.quoteText || ''}
                              onChange={(e) => {
                                updateCurrentSlideField('highlightQuote', e.target.value);
                                updateCurrentSlideField('quoteText', e.target.value);
                              }}
                              placeholder="Frase inspiradora ou citação..."
                            />
                          </div>
                          <div className="edu-edit-field-group">
                            <label className="edu-edit-label" style={{ color: '#38bdf8' }}>
                              Quadros de Destaque (01 e 02):
                            </label>
                            {ensureColumns(currentSlide).slice(0, 2).map((col, cIdx) => (
                              <div key={cIdx} className="edu-column-edit-card">
                                <div className="edu-column-edit-header">
                                  <span className="edu-column-num-badge">0{cIdx + 1}</span>
                                  <input
                                    type="text"
                                    className="edu-edit-input"
                                    style={{ fontWeight: 'bold' }}
                                    value={col.title || ''}
                                    onChange={(e) => updateCurrentSlideColumn(cIdx, 'title', e.target.value)}
                                    placeholder={`Título do Quadro 0${cIdx + 1}`}
                                  />
                                </div>
                                <textarea
                                  rows={2}
                                  className="edu-edit-input"
                                  style={{ marginTop: 4 }}
                                  value={col.description || ''}
                                  onChange={(e) => updateCurrentSlideColumn(cIdx, 'description', e.target.value)}
                                  placeholder="Descrição do quadro..."
                                />
                              </div>
                            ))}
                          </div>
                        </>
                      )}
                    </div>
                  );
                })()}
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

        {/* Right Preview Stage */}
        <main className={`edu-stage ${mobileView === 'preview' ? 'show-mobile' : 'hide-mobile'}`}>
          {/* Top Stage Bar */}
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

              <button
                type="button"
                className="edu-stage-btn"
                style={{ marginLeft: 8 }}
                onClick={() => {
                  setActiveTab('editar');
                  if (window.innerWidth < 960) setMobileView('edit');
                }}
                title="Editar textos deste slide em tempo real"
              >
                <Edit3 size={14} /> <span>Editar Textos</span>
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

          {/* 16:9 Viewport */}
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
                  showWatermark={!isPaid}
                  isSubtle={true}
                />
              )}
            </div>
          </div>
        </main>
      </div>

      {/* Mobile Bottom Bar (Sempre visível no celular/tablet, tanto no modo Editar quanto no Ver Slides) */}
      <div className="edu-mobile-bottom-bar">
        <button
          type="button"
          className="edu-btn-preview-pdf"
          style={{ flex: 1, justifyContent: 'center' }}
          onClick={() => setIsPreviewPdfOpen(true)}
        >
          <FileText size={18} />
          <span>Ver PDF</span>
        </button>

        {!isPaid ? (
          <button
            type="button"
            className="edu-btn-download-mobile"
            style={{ backgroundColor: '#10b981', flex: 2 }}
            onClick={() => setIsPaymentModalOpen(true)}
          >
            <CreditCard size={18} />
            <span>Liberar (R$ {(totalPages * 1.5).toFixed(2).replace('.', ',')})</span>
          </button>
        ) : (
          <button
            type="button"
            className="edu-btn-download-mobile"
            style={{ backgroundColor: displayPrimary, flex: 2 }}
            onClick={() => handleExportPdf()}
            disabled={isExportingPdf || !presentation}
          >
            <FileDown size={18} />
            <span>{isExportingPdf ? 'Baixando...' : 'Baixar PDF'}</span>
          </button>
        )}
      </div>

      {/* Print only container */}
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
              showWatermark={false}
            />
          </div>
        ))}
      </div>

      {/* PDF Preview Modal with Watermark */}
      <PdfPreviewModal
        isOpen={isPreviewPdfOpen}
        onClose={() => setIsPreviewPdfOpen(false)}
        presentation={presentation}
        totalPages={totalPages}
        primaryColor={displayPrimary}
        secondaryColor={displaySecondary}
        isPaid={isPaid}
        onOpenPayment={() => setIsPaymentModalOpen(true)}
        onDownloadOfficial={() => handleExportPdf()}
      />

      {/* Mercado Pago Payment Modal */}
      <PaymentModal
        isOpen={isPaymentModalOpen}
        onClose={() => setIsPaymentModalOpen(false)}
        totalPages={totalPages}
        onPaymentSuccess={handlePaymentSuccess}
        primaryColor={displayPrimary}
      />

      {/* Visual Layout Selector Modal with rich responsive previews */}
      <VisualLayoutSelectorModal
        isOpen={isLayoutModalOpen}
        onClose={() => setIsLayoutModalOpen(false)}
        pageIndex={layoutModalPageIdx}
        currentLayout={pageConfigs[layoutModalPageIdx]?.layoutType}
        onSelectLayout={(newLayoutId) => updatePageLayout(layoutModalPageIdx, newLayoutId)}
        primaryColor={displayPrimary}
        secondaryColor={displaySecondary}
      />
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
      { value: '01', label: 'PRIMEIRO PILAR', description: 'Visão geral e fundamentos essenciais do tema abordado.' },
      { value: '02', label: 'SEGUNDO PILAR', description: 'Metodologia, aplicações práticas e aprofundamento.' },
      { value: '03', label: 'TERCEIRO PILAR', description: 'Resultados esperados, conclusões e desdobramentos.' }
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
