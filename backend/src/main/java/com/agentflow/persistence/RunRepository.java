package com.agentflow.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunRepository extends JpaRepository<RunEntity, UUID> {
  Optional<RunEntity> findByTenantIdAndIdempotencyKey(
    UUID tenantId,
    String key
  );
  Optional<RunEntity> findByIdAndTenantId(UUID id, UUID tenantId);
  List<RunEntity> findTop50ByTenantIdOrderByIdDesc(UUID tenantId);
  List<RunEntity> findTop50ByStatusOrderByIdAsc(String status);
}
