export const SUBJECT_THEMES = [
  {
    id: 'matematica',
    name: 'Matemática',
    icon: '📐',
    badge: 'MATEMÁTICA & LÓGICA',
    defaultColors: ['#2563EB', '#0F172A'],
    description: 'Fórmulas, geometria, álgebra, gráficos e resolução passo a passo.',
    sampleIdea: 'Teorema de Pitágoras, relações trigonométricas no triângulo retângulo e aplicações práticas na arquitetura'
  },
  {
    id: 'portugues',
    name: 'Português & Literatura',
    icon: '📚',
    badge: 'LÍNGUA PORTUGUESA',
    defaultColors: ['#D97706', '#27272A'],
    description: 'Gramática, redação, figuras de linguagem e movimentos literários.',
    sampleIdea: 'Modernismo no Brasil: contexto histórico da Semana de Arte Moderna de 1922, principais autores e características'
  },
  {
    id: 'biologia',
    name: 'Biologia',
    icon: '🧬',
    badge: 'CIÊNCIAS BIOLÓGICAS',
    defaultColors: ['#10B981', '#064E3B'],
    description: 'Citologia, genética, ecologia, fisiologia humana e evolução.',
    sampleIdea: 'Fotossíntese e Respiração Celular: etapas fotoquímicas, ciclo de Calvin e equilíbrio energético nos ecossistemas'
  },
  {
    id: 'fisica',
    name: 'Física',
    icon: '⚛️',
    badge: 'FÍSICA & ENERGIA',
    defaultColors: ['#7C3AED', '#1E1B4B'],
    description: 'Mecânica clássica, Leis de Newton, termodinâmica, óptica e eletromagnetismo.',
    sampleIdea: 'As Três Leis de Newton, conservação da quantidade de movimento e aplicações na engenharia aeroespacial'
  },
  {
    id: 'quimica',
    name: 'Química',
    icon: '🧪',
    badge: 'QUÍMICA GERAL & ORGÂNICA',
    defaultColors: ['#06B6D4', '#0F172A'],
    description: 'Tabela periódica, ligações químicas, termoquímica e reações.',
    sampleIdea: 'Ligações Químicas Iônicas, Covalentes e Metálicas: propriedades macroscópicas e estrutura molecular'
  },
  {
    id: 'historia',
    name: 'História',
    icon: '🏛️',
    badge: 'HISTÓRIA & SOCIEDADE',
    defaultColors: ['#B45309', '#1C1917'],
    description: 'Linhas do tempo, revoluções, períodos históricos e análise social.',
    sampleIdea: 'Revolução Industrial: transformações tecnológicas, urbanização, fases históricas e impactos socioeconômicos'
  },
  {
    id: 'geografia',
    name: 'Geografia',
    icon: '🌍',
    badge: 'GEOGRAFIA & GEOPOLÍTICA',
    defaultColors: ['#0284C7', '#0C4A6E'],
    description: 'Clima, relevo, urbanização, demografia e geopolítica global.',
    sampleIdea: 'Mudanças Climáticas Globais, transição energética, matriz sustentável e impactos nos biomas brasileiros'
  },
  {
    id: 'negocios',
    name: 'Executivo & Negócios',
    icon: '💼',
    badge: 'BUSINESS PRESENTATION',
    defaultColors: ['#F97316', '#1E293B'],
    description: 'Pitch comercial, resultados financeiros, planejamento e metas.',
    sampleIdea: 'Planejamento Estratégico Anual: crescimento de mercado, transformação digital, metas trimestrais e indicadores KPI'
  },
  {
    id: 'tecnologia',
    name: 'Tecnologia & IA',
    icon: '💻',
    badge: 'TECNOLOGIA & INOVAÇÃO',
    defaultColors: ['#6366F1', '#090D16'],
    description: 'Engenharia de software, Inteligência Artificial, dados e cibersegurança.',
    sampleIdea: 'Arquitetura de Microsserviços em Nuvem, Inteligência Artificial Generativa e automação de processos'
  }
];

export const TEMPLATE_STYLES = [
  {
    id: 'geometric-modern',
    name: 'Executivo Geométrico (Referência)',
    description: 'Blocos contrastantes, molduras circulares/quadradas e tipografia forte estilo corporativo.',
    badge: 'Mais Popular'
  },
  {
    id: 'academic-clean',
    name: 'Acadêmico Didático',
    description: 'Ideal para aulas de Matemática, Biologia, Física e Português com foco em leitura clara.',
    badge: 'Educação'
  },
  {
    id: 'diagonal-dynamic',
    name: 'Diagonal Dinâmico',
    description: 'Cortes angulares modernos, faixas em movimento e forte impacto visual.',
    badge: 'Moderno'
  },
  {
    id: 'minimal-pro',
    name: 'Minimalista Editorial',
    description: 'Amplo respiro visual, cartões estruturados e contraste elegante em duas cores.',
    badge: 'Clean'
  }
];

export const COLOR_PALETTES = [
  { name: 'Laranja & Grafite (Referência)', primary: '#F97316', secondary: '#1E293B' },
  { name: 'Azul Matemática & Marinho', primary: '#2563EB', secondary: '#0F172A' },
  { name: 'Verde Biologia & Floresta', primary: '#10B981', secondary: '#064E3B' },
  { name: 'Violeta Física & Cosmos', primary: '#7C3AED', secondary: '#1E1B4B' },
  { name: 'Âmbar Português & Carvão', primary: '#D97706', secondary: '#27272A' },
  { name: 'Ciano Química & Carbono', primary: '#06B6D4', secondary: '#0F172A' },
  { name: 'Rubi & Noite', primary: '#E11D48', secondary: '#1E293B' },
  { name: 'Terracota & Pedra', primary: '#EA580C', secondary: '#292524' }
];

export const PAGE_LAYOUTS = [
  {
    id: 'cover-circle',
    name: 'Capa Geométrica com Foto Circular',
    category: 'Capa',
    supportsPhoto: true,
    description: 'Bloco na cor principal à esquerda, faixas geométricas na base e foto em moldura circular.'
  },
  {
    id: 'cover-diagonal',
    name: 'Capa Diagonal Dinâmica',
    category: 'Capa',
    supportsPhoto: true,
    description: 'Corte diagonal com faixa dupla nas 2 cores do tema e área de foto panorâmica à direita.'
  },
  {
    id: 'split-photo-left',
    name: 'Foto à Esquerda + Conteúdo Estruturado',
    category: 'Conteúdo & Foto',
    supportsPhoto: true,
    description: 'Coluna vertical colorida com foto em destaque à esquerda e texto + tópicos numerados à direita.'
  },
  {
    id: 'split-photo-right',
    name: 'Conteúdo à Esquerda + Foto à Direita',
    category: 'Conteúdo & Foto',
    supportsPhoto: true,
    description: 'Explicação detalhada e cartões de destaque à esquerda com foto emoldurada à direita.'
  },
  {
    id: 'columns-4',
    name: '4 Colunas Numeradas (01, 02, 03, 04)',
    category: 'Estrutura',
    supportsPhoto: true,
    description: 'Quatro colunas verticais numeradas alternando as 2 cores do tema para explicar pilares ou regras.'
  },
  {
    id: 'grid-4-cards',
    name: 'Grade 2x2 de Cartões com Ícones',
    category: 'Estrutura',
    supportsPhoto: true,
    description: 'Cabeçalho colorido superior e 4 cartões organizados em grade 2x2 com selos em destaque.'
  },
  {
    id: 'infographic-stats',
    name: 'Infográfico de Dados, Gráficos & KPIs',
    category: 'Dados & Fórmulas',
    supportsPhoto: true,
    description: 'Painel escuro à esquerda com gráfico de barras (ou foto) e 3 indicadores circulares à direita.'
  },
  {
    id: 'timeline-steps',
    name: 'Linha do Tempo / Passo a Passo (4 Etapas)',
    category: 'Processo',
    supportsPhoto: true,
    description: 'Fluxo horizontal conectado em 4 etapas sequenciais para processos históricos, químicos ou matemáticos.'
  },
  {
    id: 'quote-vision',
    name: 'Destaque / Fórmula + Visão & Missão',
    category: 'Conceito',
    supportsPhoto: true,
    description: 'Grande bloco de citação/fórmula à esquerda combinado com 2 cartões comparativos à direita.'
  },
  {
    id: 'closing-thankyou',
    name: 'Encerramento & Resumo Final',
    category: 'Encerramento',
    supportsPhoto: true,
    description: 'Fundo na cor secundária, blocos geométricos em 3 tons, síntese final e quadro de foto à direita.'
  }
];

