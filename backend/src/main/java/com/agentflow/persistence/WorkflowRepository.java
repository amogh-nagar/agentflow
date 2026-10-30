package com.agentflow.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepository
  extends JpaRepository<WorkflowEntity, UUID>
{
  List<WorkflowEntity> findByTenantIdOrderByNameAsc(UUID tenantId);
  Optional<WorkflowEntity> findByIdAndTenantId(UUID id, UUID tenantId);
}
