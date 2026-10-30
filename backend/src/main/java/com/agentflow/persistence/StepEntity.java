package com.agentflow.persistence;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="run_step") public class StepEntity {
 @EmbeddedId public Key id;
 public String status;
 public int attempts;
 @Column(name="output_preview") public String outputPreview;
 @Column(name="error_message") public String errorMessage;
 @Column(name="started_at") public Instant startedAt;
 @Column(name="completed_at") public Instant completedAt;
 protected StepEntity(){}
 public StepEntity(UUID run,String node){id=new Key(run,node);status="PENDING";}
 @Embeddable public static class Key implements java.io.Serializable {
  @Column(name="run_id") public UUID runId;
  @Column(name="node_id") public String nodeId;
  protected Key(){}
  public Key(UUID runId,String nodeId){this.runId=runId;this.nodeId=nodeId;}
  @Override public boolean equals(Object o){return o instanceof Key k && java.util.Objects.equals(runId,k.runId)&&java.util.Objects.equals(nodeId,k.nodeId);}
  @Override public int hashCode(){return java.util.Objects.hash(runId,nodeId);}
 }
}
