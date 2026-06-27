# Project Hub

Sistema fullstack para gerenciamento de portfólio de projetos: ciclo de vida, equipe, orçamento, classificação de risco, workflow de status e relatório consolidado.

## Stack

### Backend (`project-hub-api`)
- Java 21, Spring Boot 3.3
- Spring Web, Validation, Data JPA, Security, Actuator
- PostgreSQL, Flyway, MapStruct, Lombok
- Springdoc OpenAPI
- JUnit 5, Mockito, AssertJ, JaCoCo, Testcontainers

### Frontend (`project-hub-web`)
- React 19, TypeScript, Vite
- React Router, TanStack Query, React Hook Form, Zod
- Axios, Material UI, date-fns

### Infraestrutura
- Docker Compose
- PostgreSQL 16
- GitHub Actions (CI)

## Início rápido

```bash
cp .env.example .env
docker compose up -d --build
```

| Serviço    | URL |
|------------|-----|
| Frontend   | http://localhost:5190 |
| API        | http://localhost:8090 |
| Swagger    | http://localhost:8090/swagger-ui.html |
| Health     | http://localhost:8090/actuator/health |
| PostgreSQL | localhost:5435 |

### Credenciais de desenvolvimento

Com `SPRING_PROFILES_ACTIVE=local` (padrão no Docker Compose), seeds em `db/seed/dev/` criam:

| Usuário | Senha    | Permissões |
|---------|----------|------------|
| admin   | admin123 | CRUD, status, relatórios |
| user    | user123  | Consulta e relatórios |

## Configuração

Copie os arquivos de exemplo antes de executar:

```bash
cp .env.example .env
cp project-hub-api/.env.example project-hub-api/.env
cp project-hub-web/.env.example project-hub-web/.env
```

| Variável | Descrição |
|----------|-----------|
| `SPRING_DATASOURCE_*` | Conexão PostgreSQL |
| `PROJECT_HUB_JWT_SECRET` | Chave para assinatura JWT |
| `PROJECT_HUB_CORS_ALLOWED_ORIGINS` | Origens permitidas no CORS |
| `PROJECT_HUB_BOOTSTRAP_ADMIN_*` | Admin inicial em produção (se `app_users` vazio) |
| `SPRING_PROFILES_ACTIVE=local` | Ativa seeds de desenvolvimento |
| `VITE_API_URL` | URL da API consumida pelo frontend |

> **Docker Compose:** variáveis exportadas no shell (`SPRING_DATASOURCE_*`) têm prioridade sobre o `.env`. Remova conflitos com `unset` antes de subir os containers.

## Arquitetura

```
Controller → Service → Repository
              ↓
         Domain (risk / status)
              ↓
      DTO ↔ Mapper ↔ Entity
```

Principais decisões:
- DTOs em toda a API — sem exposição de entidades JPA
- `DefaultRiskClassifier` — regra de risco centralizada (Strategy)
- `ProjectStatusWorkflow` — transições de status validadas no domínio
- Autenticação JWT com refresh token
- Flyway — schema versionado; seeds separados por ambiente
- Integração externa de membros via `RestClient` (mock local em dev)

## Estrutura

```
project-hub/
├── docker-compose.yml
├── project-hub-api/
└── project-hub-web/
```

## Desenvolvimento local (sem Docker completo)

```bash
# Banco
docker compose up -d project-hub-db

# API
cd project-hub-api && mvn spring-boot:run

# Web
cd project-hub-web && npm ci && npm run dev
```

## Qualidade

```bash
# Backend
cd project-hub-api && mvn verify

# Frontend
cd project-hub-web && npm run lint && npm run test && npm run build
```

Cobertura mínima de 70% em `domain` e `service` (JaCoCo).

## API

Autenticação via `POST /api/auth/login` (access + refresh token).

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/projects` | Lista paginada com filtros |
| POST | `/api/projects` | Cria projeto (ADMIN) |
| PATCH | `/api/projects/{id}/status` | Altera status (ADMIN) |
| GET | `/api/reports/portfolio` | Relatório consolidado |
| GET | `/actuator/health` | Health check |

Documentação completa: `/swagger-ui.html`

## Licença

MIT
