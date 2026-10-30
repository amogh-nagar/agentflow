package com.agentflow.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface RunRepository extends JpaRepository<RunEntity,UUID> {Optional<RunEntity> findByTenantIdAndIdempotencyKey(UUID tenantId,String key);Optional<RunEntity> findByIdAndTenantId(UUID id,UUID tenantId);List<RunEntity> findTop50ByTenantIdOrderByIdDesc(UUID tenantId);List<RunEntity> findTop50ByStatusOrderByIdAsc(String status);}
