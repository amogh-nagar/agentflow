package com.agentflow.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WorkflowRepository extends JpaRepository<WorkflowEntity,UUID> {List<WorkflowEntity> findByTenantIdOrderByNameAsc(UUID tenantId);Optional<WorkflowEntity> findByIdAndTenantId(UUID id,UUID tenantId);}
