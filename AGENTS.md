# Repository Guidelines

## Project Structure & Module Organization
This repository is a Spring Boot 4 service targeting Java 21. Maven is the build tool (`pom.xml`), and the current runtime config lives in `src/main/resources/application.yaml`. Redis is the only declared local dependency, exposed through `compose.yaml`.

Keep production code under `src/main/java/com/seb/orchestrator` using the package split described in `README_Orchestrator.md`:
- `api` for controllers and HTTP DTOs
- `application` for orchestration use cases
- `domain` for models, ports, and domain services
- `infrastructure` for Redis, A2A, AI, and web adapters

Place tests in `src/test/java` mirroring the main package structure.

## Build, Test, and Development Commands
- `./mvnw spring-boot:run` runs the service locally.
- `./mvnw test` runs the test suite.
- `./mvnw clean package` builds the JAR and runs tests.
- `docker compose up -d redis` starts Redis for local session/rate-limit work.

Prefer `./mvnw` over a system Maven install so the wrapper controls the toolchain.

## Coding Style & Naming Conventions
Use 4-space indentation and standard Java naming:
- Classes: `PascalCase`
- methods and fields: `camelCase`
- constants: `UPPER_SNAKE_CASE`

Keep controllers thin. Business flow belongs in `application`, and vendor-specific code belongs in `infrastructure`. Favor small interfaces at the domain boundary over direct framework coupling. Use descriptive class names such as `ChatController`, `OrchestratorUseCase`, and `RedisSessionRepository`.

## Testing Guidelines
Use Spring Boot test starters already declared in `pom.xml`. Add unit and slice tests before full integration tests where possible. Name test classes `*Test` and keep them adjacent to the package they verify, for example `src/test/java/com/seb/orchestrator/api/ChatControllerTest.java`.

Cover session handling, rate limiting, intent routing, and response normalization first. Run `./mvnw test` before opening a PR.

## Commit & Pull Request Guidelines
Local Git history is not available in this snapshot, so no repository-specific commit convention can be inferred. Use short, imperative commit subjects such as `Add Redis-backed session port`.

PRs should include:
- a brief description of the change
- the architectural impact or tradeoff, if any
- linked issue or task reference when applicable
- request/response examples for API changes
