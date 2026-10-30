package com.agentflow.persistence;
import jakarta.persistence.*;
import java.util.UUID;
@Entity @Table(name="workflow_run") public class RunEntity {
 @Id public UUID id;
 @Column(name="tenant_id") public UUID tenantId;
 @Column(name="workflow_id") public UUID workflowId;
 @Column(name="temporal_id") public String temporalId;
 public String status;
 @Column(name="idempotency_key") public String idempotencyKey;
 @Column(name="request_hash") public String requestHash;
 protected RunEntity(){}
 public RunEntity(UUID id,UUID tenant,UUID workflow,String key,String hash){this.id=id;this.tenantId=tenant;this.workflowId=workflow;this.temporalId="agentflow-"+id;this.status="QUEUED";this.idempotencyKey=key;this.requestHash=hash;}
}
