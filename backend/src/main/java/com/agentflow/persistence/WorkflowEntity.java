package com.agentflow.persistence;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "workflow_definition")
public class WorkflowEntity {

  @Id
  public UUID id;

  @Column(name = "tenant_id", nullable = false)
  public UUID tenantId;

  public String name;
  public int version;

  @Column(name = "definition_json", columnDefinition = "text", nullable = false)
  public String definitionJson;

  protected WorkflowEntity() {}

  public WorkflowEntity(UUID id, UUID tenantId, String name, String json) {
    this.id = id;
    this.tenantId = tenantId;
    this.name = name;
    this.version = 1;
    this.definitionJson = json;
  }
}
