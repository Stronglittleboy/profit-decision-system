# Repository Guidelines

## Project Structure & Module Organization
This repository is split into `backend/` and `frontend/`, with shared docs in `docs/`, database scripts in `database/`, and local orchestration in `docker-compose.yml`.

- `backend/src/main/java/com/profit/`: Spring Boot application code
- `backend/src/main/resources/`: environment config such as `application-dev.yml`
- `backend/src/test/java/`: backend tests
- `frontend/src/`: Vue 3 app source
- `frontend/src/api/`, `views/`, `router/`, `layouts/`, `stores/`: feature-oriented frontend modules

Keep new code aligned with the current stack: Spring Boot + JDK 21 + Maven on the backend, Vue 3 + Vue Router + Element Plus on the frontend.

## Build, Test, and Development Commands

- `docker compose up -d profit-mysql profit-redis`: start local MySQL and Redis
- `cd backend && mvn spring-boot:run`: run the backend service
- `cd backend && mvn test`: execute backend tests
- `cd frontend && npm install`: install frontend dependencies
- `cd frontend && npm run dev`: start the Vite dev server
- `cd frontend && npm run build`: type-check and build the frontend
- `cd frontend && npm run typecheck`: run Vue/TypeScript checks only

## Coding Style & Naming Conventions
Use UTF-8, 4-space indentation for Java, and standard Vue SFC formatting. Prefer Lombok for boilerplate, Hutool for utility code, and MyBatis-Plus for data access.

- Java packages: `controller`, `service`, `mapper`, `entity`, `dto`, `vo`, `config`, `exception`, `common`
- Class names: `PascalCase` (`AuthController`, `DashboardSummary`)
- Methods and variables: `camelCase`
- Frontend files: `PascalCase.vue` for views/components, `camelCase.ts` for APIs and stores

## Testing Guidelines
Backend tests live under `backend/src/test/java/` and use Spring Boot’s test starter. Name tests after the target class, e.g. `AuthServiceTests`.

Prefer adding:
- controller/service tests for new backend behavior
- `npm run typecheck` before frontend commits
- `npm run build` before releasing frontend changes

## Commit & Pull Request Guidelines
Recent commits use short prefixes such as `feat:`, `fix:`, and `chore:`. Follow that style and keep subjects imperative and concise.

Pull requests should include:
- a short summary of the change
- impacted paths or modules
- verification steps run locally
- screenshots for UI changes

## Security & Configuration Tips
Do not commit secrets, local overrides, or generated artifacts. Prefer environment-specific config in `backend/src/main/resources/application-*.yml`, and keep old `jeecg` migration files untouched unless the task explicitly targets them.
