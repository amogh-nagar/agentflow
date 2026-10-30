package com.agentflow.persistence;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepRepository
  extends JpaRepository<StepEntity, StepEntity.Key>
{
  List<StepEntity> findByIdRunIdOrderByIdNodeId(UUID runId);
}
