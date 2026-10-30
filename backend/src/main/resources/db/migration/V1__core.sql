CREATE TABLE tenant (id UUID PRIMARY KEY, name VARCHAR(120) NOT NULL);
INSERT INTO tenant(id,name) VALUES ('00000000-0000-0000-0000-000000000001','Local Demo');
CREATE TABLE workflow_definition (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES tenant(id), name VARCHAR(160) NOT NULL,
 version INTEGER NOT NULL, definition_json TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_workflow_tenant ON workflow_definition(tenant_id,created_at DESC);
CREATE TABLE workflow_run (
 id UUID PRIMARY KEY, tenant_id UUID NOT NULL REFERENCES tenant(id), workflow_id UUID NOT NULL REFERENCES workflow_definition(id),
 temporal_id VARCHAR(200) NOT NULL UNIQUE, status VARCHAR(32) NOT NULL DEFAULT 'QUEUED',
 idempotency_key VARCHAR(200), request_hash VARCHAR(64), created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE (tenant_id,idempotency_key)
);
CREATE INDEX idx_run_tenant_created ON workflow_run(tenant_id,created_at DESC);
