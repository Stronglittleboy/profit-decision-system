# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**飞牛经营系统 (Profit Decision System)** — A business management system following the **Fact → Attribution → Metrics → Decision** data flow. The system tracks revenue/cost facts, attributes them to projects/customers, calculates metrics, and generates decision recommendations.

**Tech Stack:**
- Backend: Spring Boot 3.x + JDK 21 + Maven + MyBatis-Plus + Lombok + Hutool
- Frontend: Vue 3 + Vue Router + Element Plus + TypeScript + Vite
- Database: MySQL 8.0 (Flyway migrations)
- Cache: Redis 6.0
- Local infra: Docker Compose

## Development Commands

### Backend (from `backend/` directory)

```bash
# Start backend (requires MySQL + Redis running)
mvn spring-boot:run

# Build without tests
mvn clean package -DskipTests

# Run tests
mvn test
```

### Frontend (from `frontend/` directory)

```bash
# Install dependencies
npm install

# Start dev server (http://localhost:3100)
npm run dev

# Type check
npm run typecheck

# Build for production
npm run build
```

### Infrastructure

```bash
# Start MySQL + Redis
docker compose up -d profit-mysql profit-redis

# Stop services
docker compose down
```

### Testing

```bash
# Run full API integration test (requires backend running)
bash backend/src/test/scripts/api-test.sh

# Test against custom URL
bash backend/src/test/scripts/api-test.sh http://localhost:8080
```

## Architecture

### Backend Layering (DDD-inspired)

```
Controller → Application → Domain → Infrastructure
    ↓            ↓           ↓            ↓
  REST API   Use cases   Business    Persistence
  DTO/VO     Txn mgmt    rules       MyBatis
```

**Layer responsibilities:**
- **Controller** (`com.profit.controller`): HTTP endpoints, parameter validation (`@Valid`), call Application layer, return `ApiResponse<VO>`
- **Application** (`com.profit.application`): Use case orchestration, `@Transactional` boundaries, DTO↔Domain conversion, permission checks
- **Domain** (`com.profit.domain`): Aggregates, entities, value objects, domain services, repository interfaces — all business rules live here
- **Infrastructure** (`com.profit.infrastructure`): MyBatis-Plus mappers, repository implementations, external service adapters

**Key constraints:**
- Domain layer has zero dependencies on outer layers (no Spring annotations, no infrastructure)
- Application depends on Domain but not Infrastructure
- Controller only depends on Application + DTO/VO
- Infrastructure implements Domain repository interfaces

### Package Structure

```
backend/src/main/java/com/profit/
├── auth/                    # Authentication (login, token, interceptor)
├── dashboard/               # Dashboard summary queries
├── controller/              # REST endpoints
├── application/             # Use case services (e.g., ProjectAppService)
├── domain/                  # Domain models by module
│   ├── accountsubject/      # Account subject aggregate
│   ├── counterparty/        # Customer/supplier aggregate
│   ├── project/             # Project aggregate
│   ├── contract/            # Contract aggregate
│   ├── budget/              # Budget aggregate
│   └── ...
├── infrastructure/          # Persistence implementations
│   ├── accountsubject/      # AccountSubjectMapper + repo impl
│   ├── project/             # ProjectMapper + repo impl
│   └── ...
├── dto/                     # API request objects
├── vo/                      # API response objects
├── entity/                  # Database entities (MyBatis-Plus)
├── config/                  # Spring configuration
└── common/                  # ApiResponse, exception handlers, constants
```

### Frontend Structure

```
frontend/src/
├── api/                     # Backend API clients (axios)
├── views/                   # Page components (Vue SFC)
├── router/                  # Vue Router config
├── layouts/                 # Layout components (MainLayout)
├── components/              # Reusable components
├── stores/                  # State management
└── utils/                   # Utility functions
```

## Database

- **Migrations:** Flyway scripts in `backend/src/main/resources/db/migration/`
- **Naming:** Tables and columns use `snake_case`
- **Versioning:** `V{number}__{description}.sql` (e.g., `V1__create_account_subject.sql`)
- **Local DB:** Docker MySQL on port 3306, database `profit_decision_system`

**Key tables:**
- `account_subject` — Chart of accounts (tree structure)
- `counterparty` — Customers and suppliers
- `fact_event` — Revenue/cost facts (single source of truth)
- `amortization_entry` — Amortization schedule for fixed costs
- `project` — Project tracking with state machine
- `contract` — Sales/purchase contracts with state machine
- `receivable` / `payable` — AR/AP tracking
- `payment_record` — Payment history
- `budget` — Budget planning and execution tracking

## Coding Conventions

### Backend

- **Naming:** Classes use `PascalCase`, methods/variables use `camelCase`
- **DTOs:** Request objects in `dto/`, response objects in `vo/`
- **Entities:** Use Lombok (`@Data`, `@Builder`) to reduce boilerplate
- **Validation:** Use `@Valid` + JSR-303 annotations in DTOs
- **Transactions:** Apply `@Transactional` at Application layer, not Controller
- **Error handling:** `GlobalExceptionHandler` catches exceptions and returns `ApiResponse` with error codes
- **MyBatis-Plus:** Use built-in CRUD for simple queries, write custom XML for complex joins

### Frontend

- **Components:** Use `PascalCase` for component names
- **Composition API:** Prefer `<script setup>` syntax
- **TypeScript:** Enable strict type checking
- **API calls:** Centralize in `src/api/` modules
- **UI:** Use Element Plus components consistently
- **Routing:** Keep routes flat (avoid deep nesting)

### General

- **Comments:** Only add comments for non-obvious WHY (constraints, workarounds, invariants), not WHAT
- **No premature abstraction:** Three similar lines is better than a premature helper
- **Security:** Use parameterized queries, validate input at boundaries, never expose sensitive data in logs

## Key Design Patterns

### State Machines

Projects and contracts use explicit state machines:
- **Project states:** `draft` → `active` → `completed` / `cancelled`
- **Contract states:** `draft` → `active` → `completed` / `terminated`
- Transitions are validated in domain services

### Amortization

Fixed costs can be amortized over time:
- Set `amortizeStart` and `amortizeEnd` on `fact_event`
- System generates monthly `amortization_entry` records
- Query `/api/fact-event/{id}/amortization` to view schedule

### Payment Tracking

Receivables and payables track payment history:
- Create AR/AP record with `amount` and `dueDate`
- Register payments via `/api/receivable/{id}/payment` or `/api/payable/{id}/payment`
- System calculates `paidAmount`, `remainingAmount`, and `status` (pending/partial/paid/overdue)
- Batch overdue check: `/api/receivable/batch-overdue` and `/api/payable/batch-overdue`

### Reversal (冲正)

Revenue/cost facts can be reversed:
- POST `/api/fact-event/{id}/reverse` creates a reversal entry with negative amount
- Original entry is marked `isReversed=true`
- Both entries remain in the system for audit trail

## Documentation

Key docs in `docs/`:
- `requirements.md` — Business requirements and acceptance criteria
- `product-design-v2.md` — Product vision and principles
- `product-spec-v2.md` — Detailed specifications (DoD, E0-E7 pages, API contracts)
- `current-tech-plan.md` — Technical architecture and layer responsibilities
- `development-standards.md` — Coding conventions
- `domain-model.md` — DDD domain model overview
- `database-design.md` — Database schema reference
- `*-domain-design.md` — Per-module domain designs (UML, state machines, rules)
- `*-page-dsl.md` — Per-module page specifications (fields, interactions, state flows)

## Common Tasks

### Adding a new module

1. Design domain model (aggregate, entities, value objects) in `docs/{module}-domain-design.md`
2. Create Flyway migration in `backend/src/main/resources/db/migration/`
3. Implement domain layer: `com.profit.domain.{module}/`
4. Implement infrastructure: `com.profit.infrastructure.{module}/` (mapper + repo impl)
5. Implement application service: `com.profit.application.{Module}AppService`
6. Implement controller: `com.profit.controller.{Module}Controller`
7. Add frontend API client: `frontend/src/api/{module}.ts`
8. Add frontend view: `frontend/src/views/{Module}View.vue`
9. Add route in `frontend/src/router/index.ts`
10. Update API test script: `backend/src/test/scripts/api-test.sh`

### Running the full stack locally

```bash
# Terminal 1: Start infrastructure
docker compose up -d profit-mysql profit-redis

# Terminal 2: Start backend
cd backend && mvn spring-boot:run

# Terminal 3: Start frontend
cd frontend && npm run dev

# Terminal 4: Run API tests
bash backend/src/test/scripts/api-test.sh
```

### Debugging

- Backend logs: Console output from `mvn spring-boot:run`
- Frontend dev tools: Browser console + Vue DevTools
- Database: Connect to `localhost:3306`, user `profit`, password `profit123`, database `profit_decision_system`
- Redis: Connect to `localhost:6379`, no password

## Access URLs

- Frontend: http://localhost:3100
- Backend API: http://localhost:8080
- Health check: http://localhost:8080/api/health
- Dashboard: http://localhost:8080/api/dashboard/summary (requires auth)

## Authentication

- Login endpoint: `POST /api/auth/login` with `{"username":"admin","password":"123456"}`
- Returns JWT token in response: `{"data":{"token":"..."}}`
- Include token in subsequent requests: `Authorization: Bearer {token}`
- Token stored in Redis with configurable TTL
