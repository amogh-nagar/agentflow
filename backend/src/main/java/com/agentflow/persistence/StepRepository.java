package com.agentflow.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface StepRepository extends JpaRepository<StepEntity,StepEntity.Key> {List<StepEntity> findByIdRunIdOrderByIdNodeId(UUID runId);}
