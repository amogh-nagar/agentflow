# AgentFlow — Durable AI Workflow Platform

**Milestone 1: working Java + React TypeScript vertical slice.** A starter for a production-style workflow platform, not yet a production service.

## Implemented

- Java 21 / Spring Boot REST backend
- PostgreSQL persistence and Flyway migrations
- Workflow DAG validation (cycles, duplicate IDs, missing dependencies)
- Temporal Java workflow execution with dependency layers and concurrent activities
- `CONSTANT`, `UPPERCASE` demo activities, and a durable human-approval signal
- Idempotency-keyed workflow starts with a stable Temporal workflow ID
- React + TypeScript console to create definitions, run workflows, inspect state and approve
- JUnit DAG validator tests

## Prerequisites

- Java 21, Maven 3.9+, Node.js 20+, Docker, Temporal CLI
- Temporal CLI: https://docs.temporal.io/cli

## Start locally

Terminal 1:

```bash
docker compose -f infra/compose.yaml up -d
```

Terminal 2 (Temporal dev server):

```bash
temporal server start-dev
```

Terminal 3:

```bash
cd backend
mvn spring-boot:run
```

Terminal 4:

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173 (Temporal UI at http://localhost:8233).

Create the prefilled workflow, select it, click Execute, switch to Executions, then Approve the waiting workflow.

### API example

```bash
curl -X POST http://localhost:8080/api/v1/workflows -H 'Content-Type: application/json' -d '{"name":"Hello","nodes":[{"id":"hello","type":"CONSTANT","dependsOn":[],"value":"Hello AgentFlow"},{"id":"upper","type":"UPPERCASE","dependsOn":["hello"]}]}'
```

## Verification status

The repository structure and files have been generated. Maven and npm dependencies could not be installed in this sandbox, so the Java compilation, TypeScript build, and end-to-end Temporal run are **not yet verified**. Run `mvn test` and `npm run build` on your machine.

## Current limits and next milestones

**Development-only tenant:** a fixed demo tenant is used until OIDC/JWT authentication is implemented. This build is **not safe to expose publicly**.

- **P0 issue:** If PostgreSQL saves a QUEUED run but Temporal start fails, the run stays queued. Add an outbox/dispatcher that reconciles outstanding starts automatically. API retries currently return the same run without starting it again.
- Temporal is authoritative for execution status; the PostgreSQL `workflow_run.status` field is an eventually-stale projection. UI calls the Temporal query via `/runs/{id}` to get fresh status.
- Approval currently uses a signal. Upgrade to validated workflow Updates and approval timeouts.
- Add version immutability, history/step projections, auth/tenant isolation, audit events, robust request validation, AI model adapters, Kafka outbox, Redis quotas, and load/recovery tests.
- The current starter has **no LLM calls** yet; AI integrations are upcoming phases.

## Architecture

React → Spring Boot API → PostgreSQL definitions + run intents → Temporal → Java activity workers. The API and worker currently share a JVM for local convenience; they can be independently deployed later.

## Milestone 2 improvements

See [docs/MILESTONE_2.md](docs/MILESTONE_2.md). New features include queued-start recovery, step tracking (`GET /api/v1/runs/{id}/steps`), an optional LLM node, and a bounded approval wait. AI runs in mock mode by default.

For real AI calls, set `AI_MODE=openai` and `AI_API_KEY` before starting the Java app. The system intentionally does not include Redis, Kafka, or token billing yet; we are prioritizing a small working implementation.
