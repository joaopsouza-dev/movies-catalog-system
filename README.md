# 🎬 Catálogo de Filmes

Sistema de catálogo de filmes desenvolvido como trabalho final da disciplina de **Teste de Software**. O projeto permite que usuários autenticados cadastrem, consultem, atualizem e removam filmes de um catálogo pessoal, com foco na aplicação de técnicas de teste unitário, de API e E2E.

---

## 📌 Objetivo

Desenvolver um sistema simples e funcional para gerenciamento de um catálogo de filmes, aplicando testes unitários, testes de API e testes E2E, seguindo as melhores práticas de qualidade de software ensinadas na disciplina.

---

## 👥 Integrantes do Grupo

| Nome | Matrícula | GitHub |
|------|-----------|--------|
| João Pedro Souza Pereira | UC25200260 | joaopsouza-dev |
| Kenzo Matsunaga | _(preencher)_ | _(preencher)_ |
| Giulia | _(preencher)_ | _(preencher)_ |
| Kaylane | _(preencher)_ | _(preencher)_ |

---

## 🚀 Funcionalidades

- **Autenticação**: tela de login com usuário e senha.
- **CRUD de Filmes**:
  - Cadastrar novo filme (título, diretor, ano, gênero, sinopse, nota).
  - Consultar filmes cadastrados (listagem e busca por título/gênero).
  - Atualizar informações de um filme existente.
  - Remover filme do catálogo.
- **Interface de usuário**: _(frontend web ou CLI — definir)_.

---

## 🛠️ Tecnologias

> Ajuste conforme as escolhas finais do grupo.

- **Backend / API**: _(ex: Node.js + Express)_
- **Frontend**: _(ex: React, ou CLI em Node/Python)_
- **Banco de dados**: _(ex: SQLite)_
- **Autenticação**: _(ex: JWT)_

### Ferramentas de Teste

- **Testes Unitários**: _(ex: Jest)_
- **Testes de API**: _(ex: Supertest / Postman + Newman)_
- **Testes E2E**: _(ex: Cypress / Playwright)_

---

## 📂 Estrutura do Projeto

```
catalogo-filmes/
├── backend/
│   ├── src/
│   ├── tests/
│   │   ├── unit/
│   │   └── api/
│   └── package.json
├── frontend/
│   ├── src/
│   └── tests/
│       └── e2e/
├── docs/
│   ├── documento-de-visao.md
│   ├── historias-usuario.md
│   └── plano-de-testes.md
└── README.md
```

---

## ⚙️ Como Executar o Projeto

### Pré-requisitos

- Node.js (versão X ou superior) _(ajustar conforme stack)_
- _(outros pré-requisitos: banco de dados, etc.)_

### Passos

```bash
# Clonar o repositório
git clone https://github.com/<usuario>/catalogo-filmes.git
cd catalogo-filmes

# Instalar dependências do backend
cd backend
npm install

# Rodar o backend
npm start

# Instalar dependências do frontend (em outro terminal)
cd ../frontend
npm install
npm start
```

---

## ✅ Testes

### Rodando os testes unitários

```bash
cd backend
npm test
```

### Rodando os testes de API

```bash
cd backend
npm run test:api
```

### Rodando os testes E2E

```bash
cd frontend
npm run test:e2e
```

As evidências de execução (prints, logs e relatórios) estarão disponíveis em `docs/evidencias/`.

---

## 📄 Documentação

- [Documento de Visão](docs/documento-de-visao.md)
- [Histórias de Usuário (BDD)](docs/historias-usuario.md)
- [Plano de Testes](docs/plano-de-testes.md)
- [Relatório Final](docs/relatorio-final.md)

---

## 📚 Disciplina

Trabalho final desenvolvido para a disciplina de **Teste de Software**, sob orientação do(a) professor(a) _(nome)_, na Universidade Católica de Brasília (UCB).

---

## 📝 Licença

Projeto acadêmico sem fins comerciais, desenvolvido exclusivamente para fins didáticos.
