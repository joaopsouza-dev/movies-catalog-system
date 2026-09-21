# Catálogo de Filmes

Este projeto foi desenvolvido para um trabalho da disciplina de Teste de Software. A proposta não é apenas apresentar uma aplicação pronta, mas usar um sistema simples para planejar e explicar os testes que serão realizados futuramente.

O sistema escolhido foi um catálogo de filmes, pois ele possui operações comuns em muitos sistemas: cadastrar, consultar, alterar e excluir registros. Essas funcionalidades facilitam a criação de cenários de teste positivos e negativos.

## Autores

| Nome | Matrícula | GitHub |
|------|-----------|--------|
| João Pedro Souza Pereira | UC25200260 | joaopsouza-dev |
| Kenzo Matsunaga | UC25200299 | kenzomats |
| Giulia | UC25200440 | Giulia Valença de Melo - Giu_ |
| Kaylane | UC25200185 | Lanyx001 |

## Objetivo do trabalho

O objetivo principal do projeto é servir como estudo prático para testes de software. A aplicação será utilizada para planejar:

- validação de regras de negócio;
- testes de API REST;
- simulação de fluxos completos de uso;
- tratamento de dados inválidos;
- verificação de respostas esperadas e erros.

Assim, o foco do trabalho não está apenas no desenvolvimento do sistema, mas principalmente em mostrar como os testes poderão ser planejados e aplicados.

## O que o sistema faz

O sistema permite gerenciar um catálogo de filmes. Cada filme possui:

- id;
- título;
- gênero;
- ano de lançamento;
- duração em minutos.

As principais funcionalidades são:

| Funcionalidade | Descrição |
|---|---|
| Cadastrar filme | Permite adicionar um novo filme ao catálogo |
| Listar filmes | Exibe todos os filmes cadastrados |
| Buscar filme por ID | Consulta um filme específico |
| Alterar filme | Atualiza os dados de um filme existente |
| Excluir filme | Remove um filme do catálogo |
| Consultar OMDb | Busca dados de um filme em uma API externa |
| Importar OMDb | Busca um filme no OMDb e salva no catálogo |

## Regras principais do sistema

Para evitar cadastros incorretos, o sistema possui algumas validações:

- o título do filme é obrigatório;
- o gênero do filme é obrigatório;
- o ano de lançamento deve estar dentro de um intervalo válido;
- a duração deve ser maior que zero;
- não deve existir filme duplicado com o mesmo título e ano.

Essas regras serão importantes para a definição dos casos de teste unitários e dos testes de API.

## Tecnologias utilizadas

| Tecnologia | Uso no projeto |
|---|---|
| Java | Linguagem principal |
| Spring Boot | Criação da API REST |
| Spring Data JPA | Acesso ao banco de dados |
| H2 Database | Banco em memória |
| Maven | Gerenciamento do projeto |

## Estrutura resumida

```text
src/main/java/br/com/catalogofilmes
├── controller     # Endpoints da API REST
├── service        # Regras de negócio
├── repository     # Acesso ao banco de dados
├── model          # Classes que representam os dados
├── integracao     # Comunicação com a API OMDb
├── exception      # Tratamento de erros
└── cli            # Menu de linha de comando
```

O fluxo principal da aplicação é:

```text
Controller -> Service -> Repository -> Banco H2
```

No caso da importação pelo OMDb, o fluxo também passa pela camada de integração externa.

## Endpoints principais da API

| Método | Rota | Função |
|---|---|---|
| POST | `/filmes` | Cadastrar filme |
| GET | `/filmes` | Listar filmes |
| GET | `/filmes/{id}` | Buscar filme por ID |
| PUT | `/filmes/{id}` | Alterar filme |
| DELETE | `/filmes/{id}` | Excluir filme |
| GET | `/filmes/omdb?titulo=...&ano=...` | Consultar filme no OMDb sem salvar |
| POST | `/filmes/importar` | Importar filme do OMDb e salvar |

Exemplo de JSON para cadastro:

```json
{
  "titulo": "Matrix",
  "genero": "Ficção",
  "anoLancamento": 1999,
  "duracao": 136
}
```

## Planejamento de testes futuros

O planejamento de testes será dividido em três grupos principais.

### Testes unitários

Os testes unitários serão usados para validar funções e regras individuais do sistema. O foco principal será a camada de regras de negócio, responsável por processar os dados dos filmes.

Exemplos de cenários:

- cadastrar filme válido;
- impedir cadastro sem título;
- impedir cadastro duplicado;
- buscar filme existente;
- buscar filme inexistente;
- alterar filme;
- excluir filme.

### Testes de API

Os testes de API serão usados para validar os endpoints responsáveis pelas operações do catálogo. Eles deverão simular requisições HTTP e verificar se a API retorna os dados e códigos corretos.

Exemplos de cenários:

- `POST /filmes` deve retornar `201 Created`;
- `POST /filmes` com dados inválidos deve retornar `400 Bad Request`;
- `GET /filmes` deve retornar a lista de filmes;
- `GET /filmes/{id}` com ID inexistente deve retornar `404 Not Found`;
- `PUT /filmes/{id}` deve alterar os dados;
- `DELETE /filmes/{id}` deve retornar `204 No Content`.

### Testes E2E

Os testes E2E serão usados para validar o funcionamento completo do sistema simulando o uso por um usuário. A ideia será verificar um fluxo inteiro, desde a entrada dos dados até a resposta final.

Exemplos de fluxos planejados:

- cadastrar um filme e depois consultar a listagem;
- cadastrar um filme, alterar seus dados e verificar a alteração;
- cadastrar um filme, excluir e confirmar que ele não aparece mais.

## Casos de teste planejados

Os casos de teste abaixo representam uma proposta inicial para cobrir os principais comportamentos do sistema, incluindo fluxos de sucesso e fluxos de erro.

| Código | Tipo | Objetivo | Resultado esperado |
|---|---|---|---|
| CT01 | Unitário | Validar cadastro de filme válido | Filme cadastrado com sucesso |
| CT02 | Unitário | Validar campos obrigatórios | Erro de dados inválidos |
| CT03 | Unitário | Validar filme duplicado | Cadastro impedido |
| CT04 | Unitário | Validar busca de filme inexistente | Erro de filme não encontrado |
| CT05 | API | Cadastrar filme pela API | Retorno `201 Created` |
| CT06 | API | Rejeitar cadastro inválido pela API | Retorno `400 Bad Request` |
| CT07 | API | Listar filmes cadastrados | Retorno `200 OK` com a lista |
| CT08 | API | Alterar filme pela API | Dados atualizados |
| CT09 | API | Excluir filme pela API | Retorno `204 No Content` |
| CT10 | E2E | Fluxo completo de cadastro e consulta | Filme aparece na listagem |
| CT11 | E2E | Fluxo completo de alteração | Novos dados exibidos |
| CT12 | E2E | Fluxo completo de exclusão | Filme removido da listagem |

## Como executar o projeto

Para iniciar a API:

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em:

```text
http://localhost:8080
```

## API OMDb

O sistema possui integração com a API OMDb para consultar e importar filmes. A chave da API fica configurada no arquivo:

```text
src/main/resources/application.properties
```

Campo:

```properties
omdb.api-key=...
```

## Observação sobre o escopo

Apesar de o projeto possuir API REST, CLI e integração externa, o foco do trabalho está nos testes. Por isso, a explicação e os casos de teste priorizam as funcionalidades principais do catálogo de filmes.
