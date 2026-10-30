ALTER TABLE run_step ADD COLUMN output_full TEXT;
-- Outputs already truncated in earlier versions cannot be restored from this table.
