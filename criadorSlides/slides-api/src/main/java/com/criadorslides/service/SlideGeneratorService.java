package com.criadorslides.service;

import com.criadorslides.dto.*;
import com.criadorslides.model.Apresentacao;
import com.criadorslides.repository.ApresentacaoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SlideGeneratorService {

    private final ApresentacaoRepository repository;
    private final ObjectMapper objectMapper;

    public SlideGeneratorService(ApresentacaoRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    private static final List<String> DEFAULT_LAYOUT_SEQUENCE = Arrays.asList(
            "cover-circle",
            "split-photo-left",
            "columns-4",
            "infographic-stats",
            "grid-4-cards",
            "split-photo-right",
            "timeline-steps",
            "quote-vision",
            "closing-thankyou"
    );

    public PresentationResponse generatePresentation(GeneratePresentationRequest req) {
        String idea = (req.getIdea() != null && !req.getIdea().trim().isEmpty())
                ? req.getIdea().trim()
                : "Apresentação Executiva e Educacional";

        String subject = (req.getSubjectTheme() != null && !req.getSubjectTheme().trim().isEmpty())
                ? req.getSubjectTheme().trim().toLowerCase()
                : detectSubjectFromIdea(idea);

        String templateStyle = (req.getTemplateStyle() != null && !req.getTemplateStyle().trim().isEmpty())
                ? req.getTemplateStyle().trim()
                : "geometric-modern";

        int totalPages = (req.getTotalPages() != null && req.getTotalPages() >= 1 && req.getTotalPages() <= 20)
                ? req.getTotalPages()
                : (req.getPages() != null && !req.getPages().isEmpty() ? req.getPages().size() : 6);

        boolean autoColors = req.getAutoColors() == null || req.getAutoColors()
                || req.getPrimaryColor() == null || req.getPrimaryColor().trim().isEmpty()
                || req.getSecondaryColor() == null || req.getSecondaryColor().trim().isEmpty();

        String[] colors = resolveColors(autoColors, req.getPrimaryColor(), req.getSecondaryColor(), subject, idea);
        String primaryColor = colors[0];
        String secondaryColor = colors[1];

        // Map page configurations by pageNumber (1..totalPages)
        Map<Integer, SlidePageRequest> pageRequestMap = new HashMap<>();
        if (req.getPages() != null) {
            for (int i = 0; i < req.getPages().size(); i++) {
                SlidePageRequest pr = req.getPages().get(i);
                int pNum = (pr.getPageNumber() != null && pr.getPageNumber() >= 1) ? pr.getPageNumber() : (i + 1);
                pageRequestMap.put(pNum, pr);
            }
        }

        String presentationTitle = extractMainTitle(idea, subject);
        List<SlidePageDto> generatedSlides = new ArrayList<>();

        for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
            SlidePageRequest pageCfg = pageRequestMap.get(pageNum);
            String layoutType;
            if (pageCfg != null && pageCfg.getLayoutType() != null && !pageCfg.getLayoutType().trim().isEmpty()) {
                layoutType = pageCfg.getLayoutType().trim();
            } else if (pageNum == 1) {
                layoutType = "cover-circle";
            } else if (pageNum == totalPages && totalPages > 2) {
                layoutType = "closing-thankyou";
            } else {
                layoutType = DEFAULT_LAYOUT_SEQUENCE.get((pageNum - 1) % DEFAULT_LAYOUT_SEQUENCE.size());
            }

            SlidePageDto slide = buildSlideContent(
                    pageNum,
                    totalPages,
                    layoutType,
                    idea,
                    presentationTitle,
                    subject,
                    pageCfg
            );
            generatedSlides.add(slide);
        }

        PresentationResponse response = new PresentationResponse();
        response.setPresentationTitle(presentationTitle);
        response.setIdea(idea);
        response.setSubjectTheme(subject);
        response.setTemplateStyle(templateStyle);
        response.setPrimaryColor(primaryColor);
        response.setSecondaryColor(secondaryColor);
        response.setAutoColors(autoColors);
        response.setTotalPages(totalPages);
        response.setSlides(generatedSlides);

        try {
            Apresentacao entity = new Apresentacao();
            entity.setTitulo(presentationTitle);
            entity.setIdeiaOriginal(idea);
            entity.setTemaDisciplina(subject);
            entity.setModeloVisual(templateStyle);
            entity.setCorPrimaria(primaryColor);
            entity.setCorSecundaria(secondaryColor);
            entity.setCoresAutomaticas(autoColors);
            entity.setTotalPaginas(totalPages);
            // Store slides metadata in DB (strip massive base64 if over 1MB to keep DB fast, or store as JSON)
            entity.setSlidesJson(objectMapper.writeValueAsString(stripHeavyBase64ForDb(generatedSlides)));
            Apresentacao saved = repository.save(entity);
            response.setId(saved.getId());
            response.setCreatedAt(saved.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } catch (Exception e) {
            // If DB connection is temporarily unavailable in local dev, still return the generated presentation
            response.setCreatedAt(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        return response;
    }

    private List<SlidePageDto> stripHeavyBase64ForDb(List<SlidePageDto> slides) {
        List<SlidePageDto> copy = new ArrayList<>();
        for (SlidePageDto s : slides) {
            SlidePageDto c = new SlidePageDto();
            c.setPageNumber(s.getPageNumber());
            c.setLayoutType(s.getLayoutType());
            c.setBadge(s.getBadge());
            c.setTitle(s.getTitle());
            c.setSubtitle(s.getSubtitle());
            c.setBodyText(s.getBodyText());
            c.setBullets(s.getBullets());
            c.setColumns(s.getColumns());
            c.setStats(s.getStats());
            c.setHighlightQuote(s.getHighlightQuote());
            c.setPhotoCaption(s.getPhotoCaption());
            // Keep photo if reasonably sized
            if (s.getPhotoBase64() != null && s.getPhotoBase64().length() < 350_000) {
                c.setPhotoBase64(s.getPhotoBase64());
            }
            copy.add(c);
        }
        return copy;
    }

    public List<PresentationResponse> listRecentPresentations() {
        List<PresentationResponse> list = new ArrayList<>();
        try {
            List<Apresentacao> entities = repository.findTop20ByOrderByCriadoEmDesc();
            for (Apresentacao e : entities) {
                PresentationResponse r = new PresentationResponse();
                r.setId(e.getId());
                r.setPresentationTitle(e.getTitulo());
                r.setIdea(e.getIdeiaOriginal());
                r.setSubjectTheme(e.getTemaDisciplina());
                r.setTemplateStyle(e.getModeloVisual());
                r.setPrimaryColor(e.getCorPrimaria());
                r.setSecondaryColor(e.getCorSecundaria());
                r.setAutoColors(e.getCoresAutomaticas());
                r.setTotalPages(e.getTotalPaginas());
                r.setCreatedAt(e.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                if (e.getSlidesJson() != null) {
                    List<SlidePageDto> slides = objectMapper.readValue(
                            e.getSlidesJson(),
                            new TypeReference<List<SlidePageDto>>() {}
                    );
                    r.setSlides(slides);
                }
                list.add(r);
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    private String detectSubjectFromIdea(String idea) {
        String lower = idea.toLowerCase();
        if (lower.contains("matem") || lower.contains("equa") || lower.contains("geometr") || lower.contains("fração") || lower.contains("estatíst") || lower.contains("cálculo")) {
            return "matematica";
        }
        if (lower.contains("portugu") || lower.contains("gramát") || lower.contains("literatura") || lower.contains("redação") || lower.contains("verbo")) {
            return "portugues";
        }
        if (lower.contains("biolog") || lower.contains("célula") || lower.contains("ecolog") || lower.contains("genét") || lower.contains("dna") || lower.contains("fotossíntese") || lower.contains("corpo humano")) {
            return "biologia";
        }
        if (lower.contains("físic") || lower.contains("fisic") || lower.contains("newton") || lower.contains("energia") || lower.contains("eletric") || lower.contains("óptica") || lower.contains("termodinâmica")) {
            return "fisica";
        }
        if (lower.contains("químic") || lower.contains("quimic") || lower.contains("átomo") || lower.contains("tabela periódica") || lower.contains("reação") || lower.contains("molécula")) {
            return "quimica";
        }
        if (lower.contains("histór") || lower.contains("histor") || lower.contains("século") || lower.contains("guerra") || lower.contains("revolução") || lower.contains("império")) {
            return "historia";
        }
        if (lower.contains("geograf") || lower.contains("clima") || lower.contains("relevo") || lower.contains("geopolít") || lower.contains("globalização")) {
            return "geografia";
        }
        if (lower.contains("software") || lower.contains("inteligência artificial") || lower.contains("programação") || lower.contains("sistema") || lower.contains("dados") || lower.contains("tecnolog")) {
            return "tecnologia";
        }
        return "negocios";
    }

    private String[] resolveColors(boolean autoColors, String userPrimary, String userSecondary, String subject, String idea) {
        if (!autoColors && userPrimary != null && !userPrimary.isBlank() && userSecondary != null && !userSecondary.isBlank()) {
            return new String[]{normalizeHex(userPrimary, "#F97316"), normalizeHex(userSecondary, "#1E293B")};
        }

        // AI Automatic 2-Color Palette Selection based on Subject & Idea tone
        return switch (subject) {
            case "matematica" -> new String[]{"#2563EB", "#0F172A"}; // Royal Math Blue + Deep Slate
            case "portugues" -> new String[]{"#D97706", "#27272A"};  // Warm Literary Amber + Charcoal
            case "biologia" -> new String[]{"#10B981", "#064E3B"};   // Emerald Bio Green + Forest Dark
            case "fisica" -> new String[]{"#7C3AED", "#1E1B4B"};     // Quantum Violet + Cosmic Indigo
            case "quimica" -> new String[]{"#06B6D4", "#0F172A"};    // Lab Cyan + Dark Carbon
            case "historia" -> new String[]{"#B45309", "#1C1917"};   // Classic Terracotta Gold + Stone Dark
            case "geografia" -> new String[]{"#0284C7", "#0C4A6E"};  // Earth Ocean Blue + Deep Navy
            case "tecnologia" -> new String[]{"#6366F1", "#090D16"}; // Cyber Indigo + Midnight
            default -> new String[]{"#F97316", "#1E293B"};           // Executive Orange + Charcoal Dark (matches reference images!)
        };
    }

    private String normalizeHex(String hex, String fallback) {
        String clean = hex.trim();
        if (!clean.startsWith("#")) {
            clean = "#" + clean;
        }
        if (clean.matches("^#[0-9A-Fa-f]{6}$")) {
            return clean.toUpperCase();
        }
        return fallback;
    }

    private String extractMainTitle(String idea, String subject) {
        String cleaned = idea
                .replaceAll("(?i)^(crie|faça|gere| elabore|quero)\\s+(um|uma)\\s+(slide|apresentação)\\s+(sobre|de|do|da)\\s+", "")
                .replaceAll("(?i)^(slide|apresentação)\\s+(sobre|de|do|da)\\s+", "")
                .trim();
        if (cleaned.length() > 68) {
            cleaned = cleaned.substring(0, 65).trim() + "...";
        }
        if (cleaned.isEmpty()) {
            return "Apresentação de " + getSubjectDisplayName(subject);
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private String getSubjectDisplayName(String subject) {
        return switch (subject) {
            case "matematica" -> "Matemática & Lógica";
            case "portugues" -> "Língua Portuguesa & Literatura";
            case "biologia" -> "Biologia & Ciências da Vida";
            case "fisica" -> "Física & Fenômenos Naturais";
            case "quimica" -> "Química & Transformações";
            case "historia" -> "História & Sociedade";
            case "geografia" -> "Geografia & Geopolítica";
            case "tecnologia" -> "Tecnologia & Inovação";
            default -> "Estratégia & Negócios";
        };
    }

    private SlidePageDto buildSlideContent(
            int pageNum,
            int totalPages,
            String layoutType,
            String idea,
            String mainTitle,
            String subject,
            SlidePageRequest pageCfg
    ) {
        SlidePageDto dto = new SlidePageDto();
        dto.setPageNumber(pageNum);
        dto.setLayoutType(layoutType);

        if (pageCfg != null) {
            dto.setPhotoBase64(pageCfg.getPhotoBase64());
            dto.setPhotoCaption(pageCfg.getPhotoCaption());
        }

        String subjectBadge = getSubjectDisplayName(subject).toUpperCase();
        List<String> keywords = extractKeywords(idea, subject);
        String customFocus = (pageCfg != null && pageCfg.getCustomNotes() != null && !pageCfg.getCustomNotes().isBlank())
                ? pageCfg.getCustomNotes().trim()
                : null;

        switch (layoutType) {
            case "cover-circle", "cover-diagonal" -> {
                dto.setBadge(subjectBadge + " • APRESENTAÇÃO OFICIAL");
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle() : mainTitle.toUpperCase());
                dto.setSubtitle(customFocus != null ? customFocus
                        : "Análise estruturada, conceitos fundamentais e aplicações práticas sobre " + mainTitle.toLowerCase() + ".");
                dto.setBodyText("Material preparado com estrutura visual moderna 16:9, dividido em " + totalPages + " etapas estratégicas.");
                dto.setBullets(Arrays.asList(
                        "Domínio Conceitual: " + keywords.get(0),
                        "Aplicação Prática: " + keywords.get(1),
                        "Síntese & Resultados: " + keywords.get(2)
                ));
            }

            case "split-photo-left", "split-photo-right" -> {
                dto.setBadge("PÁGINA 0" + pageNum + " • " + subjectBadge);
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle()
                        : (pageNum == 2 ? "INTRODUÇÃO E CONTEXTO PRINCIPAL" : "ANÁLISE E DESENVOLVIMENTO DO TEMA"));
                dto.setSubtitle(customFocus != null ? customFocus
                        : "Compreendendo as bases e o funcionamento prático de " + mainTitle);
                dto.setBodyText(buildSubjectParagraph(subject, mainTitle, customFocus, pageNum));
                dto.setBullets(Arrays.asList(
                        "Fundamento Essencial: Definição clara e princípios que regem " + keywords.get(0).toLowerCase() + ".",
                        "Mecanismo de Ação: Como as variáveis interagem no contexto de " + mainTitle.toLowerCase() + ".",
                        "Relevância Prática: Impacto direto na resolução de problemas reais da área."
                ));
            }

            case "columns-4" -> {
                dto.setBadge("ESTRUTURA EM 4 PILARES • " + subjectBadge);
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle()
                        : "OS 4 PILARES FUNDAMENTAIS");
                dto.setSubtitle(customFocus != null ? customFocus
                        : "Decomposição didática e sequencial sobre " + mainTitle);
                dto.setColumns(buildFourColumns(subject, mainTitle, keywords));
            }

            case "grid-4-cards" -> {
                dto.setBadge("MATRIZ DE TÓPICOS • " + subjectBadge);
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle()
                        : "CONCEITOS-CHAVE E APLICAÇÕES");
                dto.setSubtitle(customFocus != null ? customFocus
                        : "Quatro dimensões essenciais para dominar " + mainTitle);
                dto.setColumns(buildFourGridCards(subject, mainTitle, keywords));
            }

            case "infographic-stats" -> {
                dto.setBadge("INDICADORES & DADOS • " + subjectBadge);
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle()
                        : "PANORAMA ANALÍTICO E PROPORÇÕES");
                dto.setSubtitle(customFocus != null ? customFocus
                        : "Métricas comparativas, distribuição e evidências sobre " + mainTitle);
                dto.setBodyText("A visualização quantitativa permite identificar padrões críticos e comprovar hipóteses fundamentais dentro do estudo de " + mainTitle.toLowerCase() + ".");
                dto.setStats(buildStatsForSubject(subject, mainTitle));
                dto.setBullets(Arrays.asList(
                        "Alta correlação entre base teórica e eficiência prática comprovada.",
                        "Padrão consistente observado nos principais estudos de " + getSubjectDisplayName(subject) + "."
                ));
            }

            case "timeline-steps" -> {
                dto.setBadge("FLUXO SEQUENCIAL • " + subjectBadge);
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle()
                        : "ETAPAS E EVOLUÇÃO DO PROCESSO");
                dto.setSubtitle(customFocus != null ? customFocus
                        : "Progressão passo a passo aplicada ao estudo de " + mainTitle);
                dto.setColumns(buildTimelineSteps(subject, mainTitle, keywords));
            }

            case "quote-vision" -> {
                dto.setBadge("DESTAQUE & SÍNTESE • " + subjectBadge);
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle()
                        : "PRINCÍPIO CENTRAL E DIRETRIZES");
                dto.setSubtitle(customFocus != null ? customFocus
                        : "Fórmula conceitual, visão e missão aplicadas ao tema");
                dto.setHighlightQuote(buildSubjectQuoteOrFormula(subject, mainTitle));
                dto.setColumns(Arrays.asList(
                        new SlidePageDto.ColumnItemDto("01", "VISÃO TEÓRICA",
                                "Compreender profundamente os fenômenos e regras estruturais que sustentam " + mainTitle.toLowerCase() + " com rigor científico."),
                        new SlidePageDto.ColumnItemDto("02", "MISSÃO PRÁTICA",
                                "Transformar o conhecimento de " + getSubjectDisplayName(subject) + " em soluções aplicáveis, críticas e mensuráveis.")
                ));
            }

            case "closing-thankyou" -> {
                dto.setBadge("CONCLUSÃO • " + subjectBadge);
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle()
                        : "OBRIGADO PELA ATENÇÃO!");
                dto.setSubtitle(customFocus != null ? customFocus
                        : "Síntese final e espaço aberto para perguntas e debate sobre " + mainTitle);
                dto.setBodyText("Revisamos os conceitos centrais, estruturas analíticas e aplicações práticas de " + mainTitle + ". Continue explorando os materiais complementares.");
                dto.setBullets(Arrays.asList(
                        "Resumo: Conceitos estruturados de " + mainTitle,
                        "Área Temática: " + getSubjectDisplayName(subject),
                        "Dúvidas, debates e considerações finais"
                ));
            }

            default -> {
                dto.setBadge("PÁGINA 0" + pageNum + " • " + subjectBadge);
                dto.setTitle(hasCustomTitle(pageCfg) ? pageCfg.getCustomTitle() : mainTitle.toUpperCase());
                dto.setSubtitle("Análise detalhada e tópicos essenciais");
                dto.setBodyText(buildSubjectParagraph(subject, mainTitle, customFocus, pageNum));
                dto.setBullets(Arrays.asList(
                        "Tópico Principal: " + keywords.get(0),
                        "Desdobramento: " + keywords.get(1),
                        "Aplicação: " + keywords.get(2)
                ));
            }
        }

        return dto;
    }

    private boolean hasCustomTitle(SlidePageRequest pageCfg) {
        return pageCfg != null && pageCfg.getCustomTitle() != null && !pageCfg.getCustomTitle().trim().isEmpty();
    }

    private List<String> extractKeywords(String idea, String subject) {
        String[] words = idea.replaceAll("[^\\p{L}\\p{N}\\s]", " ").trim().split("\\s+");
        List<String> meaningful = new ArrayList<>();
        Set<String> stopWords = Set.of("de", "da", "do", "das", "dos", "para", "com", "sem", "sobre", "um", "uma", "os", "as", "no", "na", "em", "que", "como", "por", "mais", "slide", "slides", "apresentação");
        for (String w : words) {
            if (w.length() > 3 && !stopWords.contains(w.toLowerCase())) {
                meaningful.add(Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase());
            }
        }
        if (meaningful.size() >= 4) {
            return meaningful.subList(0, 4);
        }
        return switch (subject) {
            case "matematica" -> Arrays.asList("Propriedades Algébricas", "Relações Geométricas", "Resolução Passo a Passo", "Modelagem Prática");
            case "portugues" -> Arrays.asList("Estrutura Linguística", "Interpretação Textual", "Coesão e Coerência", "Expressão Estilística");
            case "biologia" -> Arrays.asList("Organização Celular", "Metabolismo e Energia", "Interação Ecológica", "Evolução e Genética");
            case "fisica" -> Arrays.asList("Leis do Movimento", "Conservação de Energia", "Interações de Campo", "Experimentação Prática");
            case "quimica" -> Arrays.asList("Estrutura Atômica", "Ligações Químicas", "Estequiometria", "Equilíbrio e Reações");
            case "historia" -> Arrays.asList("Contexto Histórico", "Atores Sociais", "Transformações Políticas", "Legado Cultural");
            case "geografia" -> Arrays.asList("Dinâmica Territorial", "Fatores Climáticos", "Fluxos Econômicos", "Sustentabilidade");
            case "tecnologia" -> Arrays.asList("Arquitetura de Sistemas", "Automação Inteligente", "Escalabilidade", "Segurança Digital");
            default -> Arrays.asList("Planejamento Estratégico", "Execução Operacional", "Indicadores de Desempenho", "Inovação Contínua");
        };
    }

    private String buildSubjectParagraph(String subject, String mainTitle, String customFocus, int pageNum) {
        if (customFocus != null && !customFocus.isEmpty()) {
            return customFocus + " — Dentro do estudo de " + mainTitle + ", este ponto destaca-se pela importância direta na compreensão global do tema e na aplicação em cenários reais.";
        }
        return switch (subject) {
            case "matematica" -> "Na Matemática, o estudo de " + mainTitle + " permite demonstrar relações exatas entre grandezas, padrões numéricos e estruturas espaciais. Compreender sua dedução lógica facilita a resolução de problemas complexos.";
            case "portugues" -> "No âmbito da Língua Portuguesa e Literatura, " + mainTitle + " evidencia como a estrutura do discurso, a semântica e o contexto histórico-cultural constroem sentidos ricos e articulados na comunicação.";
            case "biologia" -> "Nas Ciências Biológicas, " + mainTitle + " envolve sistemas vivos altamente integrados, onde processos bioquímicos, celulares e ambientais atuam em equilíbrio dinâmico para manter a vida e a biodiversidade.";
            case "fisica" -> "Na Física, a análise de " + mainTitle + " fundamenta-se na observação experimental e na formulação matemática das leis naturais que descrevem matéria, energia, forças e movimento no universo.";
            case "quimica" -> "Na Química, " + mainTitle + " explora a composição da matéria, as interações intermoleculares e os balanços energéticos que governam as transformações das substâncias.";
            case "historia" -> "Na análise histórica, " + mainTitle + " resulta da convergência de fatores econômicos, políticos e culturais que moldaram as instituições humanas ao longo do tempo.";
            case "geografia" -> "Sob a ótica geográfica, " + mainTitle + " conecta a dinâmica socioespacial às características físicas do território, revelando fluxos populacionais, ambientais e geopolíticos.";
            case "tecnologia" -> "No ecossistema de Tecnologia, " + mainTitle + " combina arquitetura moderna, algoritmos eficientes e experiência do usuário para resolver desafios em alta escala.";
            default -> "O desenvolvimento estratégico de " + mainTitle + " exige alinhamento entre visão de longo prazo, processos bem definidos e análise de indicadores para gerar resultados consistentes.";
        };
    }

    private List<SlidePageDto.ColumnItemDto> buildFourColumns(String subject, String mainTitle, List<String> kw) {
        return Arrays.asList(
                new SlidePageDto.ColumnItemDto("01", kw.get(0).toUpperCase(),
                        "Definição estrutural e conceitos de base essenciais para introduzir " + mainTitle.toLowerCase() + " com clareza."),
                new SlidePageDto.ColumnItemDto("02", kw.get(1).toUpperCase(),
                        "Articulação entre variáveis, regras principais e mecanismos que determinam o comportamento do tema."),
                new SlidePageDto.ColumnItemDto("03", kw.get(2).toUpperCase(),
                        "Exemplos contextualizados, demonstrações práticas e casos reais estudados em " + getSubjectDisplayName(subject) + "."),
                new SlidePageDto.ColumnItemDto("04", kw.get(3).toUpperCase(),
                        "Síntese crítica, verificação de resultados e recomendações para consolidação do aprendizado.")
        );
    }

    private List<SlidePageDto.ColumnItemDto> buildFourGridCards(String subject, String mainTitle, List<String> kw) {
        return Arrays.asList(
                new SlidePageDto.ColumnItemDto("A", "Fundamentação Teórica",
                        "Princípios essenciais e leis que regem " + mainTitle.toLowerCase() + " na disciplina de " + getSubjectDisplayName(subject) + "."),
                new SlidePageDto.ColumnItemDto("B", "Metodologia de Análise",
                        "Ferramentas, fórmulas e critérios utilizados para investigar e validar cada etapa do processo."),
                new SlidePageDto.ColumnItemDto("C", "Exemplos Aplicados",
                        "Situações reais do cotidiano e exercícios práticos onde " + kw.get(0).toLowerCase() + " se manifesta diretamente."),
                new SlidePageDto.ColumnItemDto("D", "Impacto e Conclusões",
                        "Principais aprendizados, pontos de atenção e conexões interdisciplinares do tema.")
        );
    }

    private List<SlidePageDto.ColumnItemDto> buildTimelineSteps(String subject, String mainTitle, List<String> kw) {
        return Arrays.asList(
                new SlidePageDto.ColumnItemDto("ETAPA 1", "Identificação & Base",
                        "Reconhecimento dos elementos iniciais e levantamento dos dados fundamentais de " + mainTitle.toLowerCase() + "."),
                new SlidePageDto.ColumnItemDto("ETAPA 2", "Processamento & Regras",
                        "Aplicação dos princípios de " + getSubjectDisplayName(subject) + " sobre " + kw.get(0).toLowerCase() + "."),
                new SlidePageDto.ColumnItemDto("ETAPA 3", "Validação Prática",
                        "Conferência de hipóteses, testes comparativos e resolução de casos práticos."),
                new SlidePageDto.ColumnItemDto("ETAPA 4", "Consolidação Final",
                        "Síntese dos resultados obtidos e aplicação segura em novos problemas.")
        );
    }

    private List<SlidePageDto.StatItemDto> buildStatsForSubject(String subject, String mainTitle) {
        return switch (subject) {
            case "matematica" -> Arrays.asList(
                    new SlidePageDto.StatItemDto("95%", "Precisão Lógica", "Exatidão na modelagem e resolução de problemas com método estruturado."),
                    new SlidePageDto.StatItemDto("3x", "Velocidade Analítica", "Ganho de agilidade ao dominar as propriedades fundamentais do tema."),
                    new SlidePageDto.StatItemDto("100%", "Aplicabilidade", "Presente em exames, engenharias, finanças e ciências exatas.")
            );
            case "biologia" -> Arrays.asList(
                    new SlidePageDto.StatItemDto("78%", "Equilíbrio Sistêmico", "Interdependência direta entre processos celulares e o ecossistema."),
                    new SlidePageDto.StatItemDto("92%", "Conservação Biológica", "Eficiência energética observada nos ciclos naturais estudados."),
                    new SlidePageDto.StatItemDto("4 Fases", "Ciclo Funcional", "Etapas integradas de regulação, adaptação e reprodução.")
            );
            case "fisica", "quimica" -> Arrays.asList(
                    new SlidePageDto.StatItemDto("98%", "Conservação", "Correspondência entre previsão teórica e medição experimental em laboratório."),
                    new SlidePageDto.StatItemDto("4 Leis", "Princípios Base", "Relações fundamentais que explicam o comportamento energético do sistema."),
                    new SlidePageDto.StatItemDto("85%", "Eficiência Prática", "Aproveitamento energético e transformação controlada nos processos.")
            );
            default -> Arrays.asList(
                    new SlidePageDto.StatItemDto("89%", "Relevância Prática", "Impacto direto do domínio de " + mainTitle + " nos resultados finais."),
                    new SlidePageDto.StatItemDto("43%", "Crescimento & Escala", "Aumento de eficiência com metodologia estruturada em etapas claras."),
                    new SlidePageDto.StatItemDto("94%", "Consistência", "Taxa de retenção visual e clareza na comunicação executiva e educacional.")
            );
        };
    }

    private String buildSubjectQuoteOrFormula(String subject, String mainTitle) {
        return switch (subject) {
            case "matematica" -> "“A Matemática é o alfabeto com o qual a lógica estrutura o universo: compreender " + mainTitle + " é transformar abstração em clareza exata.”";
            case "fisica" -> "“Na natureza nada se perde, tudo se transforma e obedece a princípios mensuráveis: " + mainTitle + " revela a harmonia entre força, matéria e energia.”";
            case "biologia" -> "“Em Biologia, cada estrutura cumpre uma função vital integrada: entender " + mainTitle + " é compreender a própria dinâmica da vida.”";
            case "quimica" -> "“A transformação da matéria em nível molecular explica os fenômenos macroscópicos que observamos em " + mainTitle + ".”";
            case "portugues" -> "“Dominar a linguagem e suas estruturas em " + mainTitle + " amplia a capacidade crítica de interpretar e transformar o mundo.”";
            case "historia" -> "“Compreender as raízes históricas de " + mainTitle + " é essencial para interpretar os desafios do presente e projetar o futuro.”";
            default -> "“A excelência em " + mainTitle + " nasce da combinação entre visão clara, método estruturado e execução consistente.”";
        };
    }
}

