# Portfólio de Projetos

API REST para gerenciar o portfólio de projetos de uma empresa, cobrindo o ciclo de vida completo de cada projeto — da análise de viabilidade à finalização — incluindo gerenciamento de equipe, orçamento e classificação de risco.

Principais recursos: CRUD de projetos com paginação e filtros, transição de status validada por sequência, classificação de risco dinâmica, alocação de membros (via API externa mockada) e relatório resumido do portfólio.

## Tecnologias

- **Java 21** + **Spring Boot 3.3** (arquitetura MVC)
- **Spring Web**, **Spring Data JPA + Hibernate**, **Spring Security** (HTTP Basic), **Bean Validation**
- **PostgreSQL** (produção, via Docker) · **H2** (testes unitários)
- **Flyway** (migrations versionadas do schema)
- **Spring Boot Actuator** (health check)
- **springdoc-openapi** (Swagger UI)
- **JUnit 5 + Mockito + JaCoCo** (cobertura ≥ 70% nas regras de negócio) · **Testcontainers** (testes de integração)
- **Docker** + **Docker Compose** (build e execução)

## Pré-requisitos

- **Docker** e **Docker Compose** instalados.

Não é necessário ter Java ou Maven na máquina: o build é feito dentro do `Dockerfile` (multi-stage), que baixa as dependências, compila o projeto e gera a imagem da API.

## Como executar

Na raiz do projeto, suba o banco e a API juntos:

```bash
docker compose up --build -d
```

- O Compose sobe dois serviços: `postgres` e `api`.
- A API só inicia após o banco ficar **healthy** (`depends_on` + healthcheck).
- Acompanhe a inicialização com `docker compose logs -f api` (aguarde `Started PortfolioApplication`).

A API ficará disponível em `http://localhost:8080`.

O schema do banco é criado/versionado automaticamente pelo **Flyway** na subida (migrations em `src/main/resources/db/migration`). O health check da API fica em `http://localhost:8080/actuator/health` (também usado pelo healthcheck do container).

Para parar:

```bash
docker compose down       # para os containers (mantém os dados)
docker compose down -v    # para e apaga o volume do banco
```

> Após alterar o código, rode novamente `docker compose up --build -d` para reconstruir a imagem.

## Autenticação

Todos os endpoints (exceto o Swagger) exigem **HTTP Basic** com usuário em memória:

- usuário: `admin`
- senha: `admin123`

## Documentação (Swagger / OpenAPI)

Com a aplicação no ar:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Todos os endpoints ficam sob o prefixo `/api/v1` (ex.: `/api/v1/projects`, `/api/v1/external/members`, `/api/v1/reports/portfolio`).

Exemplo rápido:

```bash
curl -u admin:admin123 http://localhost:8080/api/v1/reports/portfolio
```

### Postman

Além do Swagger, você pode importar a collection pronta no Postman: **`portfolio.postman_collection.json`** (na raiz do projeto).

Em *Import → File*, selecione o arquivo. A collection já vem com:

- a autenticação Basic (`admin`/`admin123`) e a variável `baseUrl` (`http://localhost:8080`) configuradas;
- exemplos de body para todos os endpoints;
- scripts que capturam automaticamente os IDs criados (gerente, funcionário e projeto), bastando executar os requests na ordem das pastas.

## Conectar ao banco de dados

O PostgreSQL é exposto na porta **5432** do host. Use qualquer cliente (psql, DBeaver, pgAdmin, IntelliJ) com:

| Parâmetro | Valor       |
|-----------|-------------|
| Host      | `localhost` |
| Porta     | `5432`      |
| Database  | `portfolio` |
| Usuário   | `portfolio` |
| Senha     | `portfolio` |

```bash
psql "postgresql://portfolio:portfolio@localhost:5432/portfolio"
```

## Variáveis de ambiente (opcionais)

| Variável         | Padrão                                            |
|------------------|---------------------------------------------------|
| `DB_URL`         | `jdbc:postgresql://postgres:5432/portfolio`       |
| `DB_USERNAME`    | `portfolio`                                       |
| `DB_PASSWORD`    | `portfolio`                                       |
| `APP_USER`       | `admin`                                           |
| `APP_PASSWORD`   | `admin123`                                        |
| `MEMBER_API_URL` | `http://localhost:8080/api/v1/external/members`   |

## Testes

São dois níveis:

- **Testes unitários** — cobrem as regras de negócio (cálculo de risco, transições de status, alocação de membros e relatório) usando **H2 em memória**. Não precisam do PostgreSQL no ar.
- **Testes de integração** (`*IT`) — sobem um **PostgreSQL real via Testcontainers**, aplicam as migrations do Flyway e validam persistência, consultas JPQL e o schema (`ddl-auto: validate`). Exigem um daemon Docker disponível na máquina.

### Rodar os testes unitários no Docker (sem Java/Maven local)

```bash
docker build --target test --progress=plain -t portfolio-tests .
```

O stage executa `mvn verify -DskipITs`: roda os testes unitários e aplica o gate de cobertura do **JaCoCo (≥ 70% no pacote de regras de negócio)**. Os testes de integração são pulados aqui, pois o Testcontainers precisa de um daemon Docker (indisponível durante o `docker build`).

### Rodar a suíte completa (unitários + integração)

Com Docker rodando na máquina e usando o Maven Wrapper:

```bash
./mvnw verify
```

> O `docker compose up --build` que sobe a API **não** executa os testes (usa o stage de empacotamento).
