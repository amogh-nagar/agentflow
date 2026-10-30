ALTER TABLE workflow_run ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT now();
CREATE TABLE run_step (
  run_id UUID NOT NULL REFERENCES workflow_run(id) ON DELETE CASCADE,
  node_id VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  attempts INTEGER NOT NULL DEFAULT 0,
  output_preview TEXT,
  error_message TEXT,
  started_at TIMESTAMPTZ,
  completed_at TIMESTAMPTZ,
  PRIMARY KEY (run_id, node_id)
);
CREATE INDEX idx_run_step_run ON run_step(run_id);
