# Milestone 3 — Visual workflows and multi-agent execution

This archive extends Milestone 2. Existing API endpoints and PostgreSQL schema are unchanged.

## New capabilities

1. **Visual builder:** Create nodes by clicking the palette or dropping a palette item on the graph; select a node to edit its type, prompt and dependency checkboxes. A node-edge SVG layout reflects dependencies. Switch to JSON for advanced editing. Node positions are auto-laid-out, not freely repositionable.
2. **Parallel AI agents:** `AGENT` is an alias of the LLM activity with a role-specific prompt. DAG nodes with shared upstream dependencies execute concurrently via Temporal Async functions. A downstream synthesis step receives **all** dependency outputs, labeled by node ID.
3. **Live-ish run view:** Run-step panel automatically refreshes every 2 seconds, displaying attempts, status, output previews, and errors. This is polling, **not SSE** yet.
4. **Recovery demonstration:** `FAIL_ONCE` intentionally throws on the first attempt and succeeds on the next Temporal activity retry. It is for demonstration, not real production workflows.
5. **Research template:** `examples/research-council.json` or click the visual builder's “Load research template”. Two agents run in parallel, synthesize results, then wait for approval.

## Run locally

From the repository root:

```bash
docker compose -f infra/compose.yaml up -d
temporal server start-dev
```

In other terminals:

```bash
cd backend && mvn test && mvn spring-boot:run
cd frontend && npm install && npm run build && npm run dev
```

Navigate to http://localhost:5173, Create workflow, Load research template, Save, Execute, Inspect. In `mock` mode no API key is needed; to use OpenAI export `AI_MODE=openai`, `AI_API_KEY`, `AI_MODEL` before launching Spring Boot.

## Failure demo

Create a workflow with `CONSTANT` followed by `FAIL_ONCE`. Execute and inspect the step; its attempts should be `2` and status `COMPLETED` when Temporal retries successfully. To test durability across restarts, create an approval workflow, wait for `WAITING_APPROVAL`, terminate Java, restart Java, and approve the pending run.

## Verification / limitations

- Generated from the Milestone 2 archive with code-level checks. **Maven compilation, npm install/build and end-to-end runtime tests were not completed in this environment.**
- Visual graph is auto-arranged; dependency edits are interactive, but drawing edges by dragging ports is not implemented.
- The workflow view uses 2-second polling. True SSE and token/cost analytics remain future work.
- Existing demo tenancy has no authentication. **Do not expose this build publicly with OpenAI enabled.**
- `FAIL_ONCE` depends on persisted step-attempt tracking. It intentionally causes one retry and must not be used for business side effects.
- Output previews are truncated to 500 characters and are not a secure redaction mechanism; avoid sensitive data.
