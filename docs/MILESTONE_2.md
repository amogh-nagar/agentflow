# Milestone 2 — deliberately small vertical slice

Implemented in this iteration:

- Queued run records survive API/Temporal dispatch failure. A scheduled retry uses stable Temporal Workflow IDs; duplicate calls retain the same run.
- Activity step tracking in Postgres (status, attempts, timestamps, truncated output, failure message). `/api/v1/runs/{id}/steps` returns data.
- `LLM` DAG node using a mock AI provider by default. Optional OpenAI-compatible chat completion using environment variables (never commit keys).
- Human approval status is persisted and a 24-hour durable timeout is enforced; a non-pending approval receives HTTP 409.
- DAG tests cover a valid LLM pipeline and cyclic dependency rejection.

## Limitations (intentionally deferred)

- The recovery loop is best suited to one API deployment; multi-instance claiming/leases and batch scheduling are future work.
- A start can race the projection update, but stable Temporal IDs prevent two executions with the same run ID.
- Activity results are limited to a 500-character preview in Postgres. Full results live in Temporal history.
- LLM retries can duplicate provider calls if a worker crashes after the provider responds but before activity completion.
- The AI adapter does not yet persist provider token usage or validate model JSON output; those are follow-on tasks.
- A cached `WAITING_APPROVAL` check does not provide atomic approval acceptance; signals are first-write-wins inside the Temporal workflow, but HTTP acknowledgment means signal accepted for delivery, not definitive approval outcome.
- Fixed demo tenant; no production authentication or per-tenant isolation yet.
- Tests are unit-level until Docker/Temporal integration tests are added.

## Setup

Follow root README. `AI_MODE=mock` works without a key. For real calls set `AI_MODE=openai` and `AI_API_KEY`; optional `AI_MODEL` and `AI_ENDPOINT` (HTTPS only). Use an account with API billing enabled.
