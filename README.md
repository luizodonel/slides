# SlideCraft AI — Criador de Slides 16:9 & Exportador PDF

Plataforma full-stack para criação inteligente de apresentações de slides em formato **16:9 Widescreen** com exportação direta para **PDF**, desenvolvida com **Java 17 (Spring Boot 3)** e **React 19 (Vite)**.

---

## 🚀 Funcionalidades

1. **Geração por Ideia & Matéria/Disciplina**:
   - O usuário digita a ideia ou tema do slide e escolhe a disciplina: **Matemática**, **Português & Literatura**, **Biologia**, **Física**, **Química**, **História**, **Geografia**, **Executivo & Negócios** ou **Tecnologia & IA**.
   - A IA/motor gera títulos, subtítulos, resumos conceituais, 4 colunas (`01` a `04`), matriz 2x2, linha do tempo/processo, dados estatísticos e sínteses adequadas ao tema.

2. **Controle Total de Páginas e Layouts por Posição**:
   - Defina a quantidade de páginas do slide (`2` a `15` páginas).
   - Escolha o modelo visual de cada página individualmente (`Página 1`, `Página 2`, ..., `Página N`).
   - Reordene a sequência de páginas com botões de subir (`↑`) e descer (`↓`).
   - 10 modelos visuais disponíveis:
     - `cover-circle`: Capa moderna com blocos geométricos e foto circular
     - `cover-diagonal`: Capa com corte diagonal e faixas nas 2 cores
     - `split-photo-left`: Coluna vertical colorida + foto à esquerda e tópicos à direita
     - `split-photo-right`: Conteúdo à esquerda + foto emoldurada à direita
     - `columns-4`: 4 colunas verticais numeradas (`01`, `02`, `03`, `04`)
     - `grid-4-cards`: Grade 2x2 de cartões com ícones/selos
     - `infographic-stats`: Painel escuro com gráfico de barras/foto e 3 indicadores com anéis
     - `timeline-steps`: Linha do tempo conectada em 4 etapas
     - `quote-vision`: Grande bloco de citação/fórmula + cartões Visão e Missão
     - `closing-thankyou`: Encerramento com blocos geométricos em 3 tons e síntese final

3. **Upload de Fotos com Vinculação por Página**:
   - Faça upload de fotos do computador/celular.
   - Selecione em qual página cada foto deve aparecer (`Exibir na Página 1`, `Página 2`, etc.).
   - Recorte automático inteligente (*object-cover*) dentro das molduras geométricas de cada layout.

4. **Tema em 2 Cores ou IA Automática**:
   - Escolha 2 cores personalizadas (`Cor Principal` e `Cor Secundária`).
   - Ou ative o modo automático onde a IA seleciona o contraste ideal de acordo com a matéria/ideia.

5. **Exportação em PDF Full HD (1920x1080)**:
   - Renderização gráfica em alta fidelidade via Apache PDFBox e Java 2D.
   - Download imediato de arquivo `.pdf` em proporção 16:9 paisagem pronta para apresentação.

---

## 🛠️ Tecnologias

- **Backend**: Java 17, Spring Boot 3.2.5, Spring Data JPA, Apache PDFBox 2.0.31, PostgreSQL.
- **Frontend**: React 19, Vite, Lucide Icons, CSS responsivo moderno.
- **Infraestrutura**: Dockerfile multi-stage (Backend & Frontend), Nginx e Docker Compose.

---

## 🐳 Executando com Docker Compose

```bash
docker compose up -d --build
```

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api/slides`
