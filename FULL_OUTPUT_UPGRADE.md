# AgentFlow — Full AI Output Upgrade

- Generated responses now use configurable `AI_MAX_OUTPUT_TOKENS` (default 4000, previously 300). Set a lower value if using a constrained provider/budget. Provider output is still bounded by context and provider limits.
- HTTP request timeout increased to 90 seconds; Temporal activity Start-to-Close timeout increased to 120 seconds.
- Flyway V3 adds `run_step.output_full TEXT`. Each completed activity now stores its full returned string plus the original 500-character preview.
- API `GET /api/v1/runs/{runId}/steps/{nodeId}/output` returns `{"output":"..."}`. Full output is not included in the 2-second polling list.
- The React step card has **View full response** and **Collapse** controls.
- Older runs will not have `output_full`, since old Postgres rows only stored previews. Create a fresh execution to see the full response.
- Note: all output is stored in plaintext. Do not deploy publicly until authentication/authorization, sensitive-output filtering, output-size limits, and retention policies are implemented. Large inputs/results can also increase Temporal history storage.
- Run `mvn test` in backend and `npm install && npm run build` in frontend, then start services and execute a new workflow.
