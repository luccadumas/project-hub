# Project Hub

Fullstack application for project portfolio management: lifecycle, team allocation, budget, risk classification, status workflow, and consolidated reporting.

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

### Infrastructure
- Docker Compose
- PostgreSQL 16
- GitHub Actions (CI)

## Quick start

```bash
cp .env.example .env
docker compose up -d --build
```

| Service    | URL |
|------------|-----|
| Frontend   | http://localhost:5190 |
| API        | http://localhost:8090 |
| Swagger    | http://localhost:8090/swagger-ui.html |
| Health     | http://localhost:8090/actuator/health |
| PostgreSQL | localhost:5435 |

### Development credentials

With `SPRING_PROFILES_ACTIVE=local` (default in Docker Compose), seeds in `db/seed/dev/` create:

| User  | Password | Permissions |
|-------|----------|-------------|
| admin | admin123 | CRUD, status changes, reports |
| user  | user123  | Read-only access and reports |

## Configuration

Copy the example files before running:

```bash
cp .env.example .env
cp project-hub-api/.env.example project-hub-api/.env
cp project-hub-web/.env.example project-hub-web/.env
```

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_*` | PostgreSQL connection |
| `PROJECT_HUB_JWT_SECRET` | JWT signing secret |
| `PROJECT_HUB_CORS_ALLOWED_ORIGINS` | Allowed CORS origins |
| `PROJECT_HUB_BOOTSTRAP_ADMIN_*` | Initial admin in production (when `app_users` is empty) |
| `SPRING_PROFILES_ACTIVE=local` | Enables development seeds |
| `VITE_API_URL` | API URL used by the frontend |

> **Docker Compose:** shell-exported variables (`SPRING_DATASOURCE_*`) override `.env`. Run `unset` on conflicting values before starting containers.

## Architecture

```
Controller → Service → Repository
              ↓
         Domain (risk / status)
              ↓
      DTO ↔ Mapper ↔ Entity
```

Key decisions:
- DTOs across the API — no JPA entity exposure
- `DefaultRiskClassifier` — centralized risk rules (Strategy)
- `ProjectStatusWorkflow` — status transitions validated in the domain
- JWT authentication with refresh tokens
- Flyway — versioned schema; seeds separated by environment
- External member integration via `RestClient` (local mock in dev)

## Structure

```
project-hub/
├── docker-compose.yml
├── project-hub-api/
└── project-hub-web/
```

## Local development (without full Docker)

```bash
# Database
docker compose up -d project-hub-db

# API
cd project-hub-api && mvn spring-boot:run

# Web
cd project-hub-web && npm ci && npm run dev
```

## Quality

```bash
# Backend
cd project-hub-api && mvn verify

# Frontend
cd project-hub-web && npm run lint && npm run test && npm run build
```

Minimum coverage of 70% in `domain` and `service` (JaCoCo).

## API

Authenticate via `POST /api/auth/login` (access + refresh token).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | Paginated list with filters |
| POST | `/api/projects` | Create project (ADMIN) |
| PATCH | `/api/projects/{id}/status` | Update status (ADMIN) |
| GET | `/api/reports/portfolio` | Consolidated report |
| GET | `/actuator/health` | Health check |

Full documentation: `/swagger-ui.html`

## License

MIT
