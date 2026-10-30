package com.agentflow.service;
import com.agentflow.domain.*;
import com.agentflow.persistence.*;
import com.agentflow.temporal.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.temporal.client.*;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
@Service public class WorkflowService {
 private final WorkflowRepository workflows;private final RunRepository runs;private final StepRepository steps;private final ObjectMapper mapper;
 private final WorkflowClient temporal;private final DagValidator validator;
 @Value("${agentflow.demo-tenant}") private UUID tenant;
 @Value("${agentflow.temporal.task-queue}") private String queue;
 public WorkflowService(WorkflowRepository w,RunRepository r,StepRepository steps,ObjectMapper m,WorkflowClient t,DagValidator v){workflows=w;runs=r;this.steps=steps;mapper=m;temporal=t;validator=v;}
 public WorkflowEntity create(WorkflowSpec spec){validator.validate(spec);try{return workflows.save(new WorkflowEntity(UUID.randomUUID(),tenant,spec.name(),mapper.writeValueAsString(spec)));}catch(Exception e){throw new IllegalStateException("Could not save workflow",e);}}
 public List<WorkflowEntity> list(){return workflows.findByTenantIdOrderByNameAsc(tenant);}
 public WorkflowEntity get(UUID id){return workflows.findByIdAndTenantId(id,tenant).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Workflow not found"));}
 public RunEntity start(UUID workflowId,String idempotencyKey){
  get(workflowId);
  if(idempotencyKey==null||idempotencyKey.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Idempotency-Key header required");
  if(idempotencyKey.length()>200)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Idempotency key too long");
  var existing=runs.findByTenantIdAndIdempotencyKey(tenant,idempotencyKey);
  if(existing.isPresent()){
   if(!existing.get().workflowId.equals(workflowId))throw new ResponseStatusException(HttpStatus.CONFLICT,"Key used by another workflow");
   dispatch(existing.get());return existing.get();
  }
  var run=new RunEntity(UUID.randomUUID(),tenant,workflowId,idempotencyKey,sha256(workflowId.toString()));
  try{runs.saveAndFlush(run);}catch(org.springframework.dao.DataIntegrityViolationException e){
   var previous=runs.findByTenantIdAndIdempotencyKey(tenant,idempotencyKey).orElseThrow(()->e);
   if(!previous.workflowId.equals(workflowId))throw new ResponseStatusException(HttpStatus.CONFLICT,"Key used by another workflow");
   return previous;
  }
  dispatch(run);return run;
 }
 // Repeated start is safe because the Temporal Workflow ID is stable.
 // If Temporal is down, QUEUED is retained for the recovery job.
 public void dispatch(RunEntity run){
  if(!"QUEUED".equals(run.status))return;
  try{
   var def=get(run.workflowId);
   var stub=temporal.newWorkflowStub(ExecutionWorkflow.class,WorkflowOptions.newBuilder().setWorkflowId(run.temporalId).setTaskQueue(queue).build());
   WorkflowClient.start(stub::execute,mapper.readValue(def.definitionJson,WorkflowSpec.class));
   run.status="RUNNING";runs.save(run);
  }catch(WorkflowExecutionAlreadyStarted already){run.status="RUNNING";runs.save(run);
  }catch(Exception e){org.slf4j.LoggerFactory.getLogger(getClass()).warn("Dispatch pending for run {}: {}",run.id,e.toString());}
 }
 public RunEntity run(UUID id){return runs.findByIdAndTenantId(id,tenant).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Run not found"));}
 public List<RunEntity> listRuns(){return runs.findTop50ByTenantIdOrderByIdDesc(tenant);}
 public Map<String,Object> status(UUID id){var r=run(id);String state=r.status;String temporalStatus="UNKNOWN";
  try{
   var stub=temporal.newUntypedWorkflowStub(r.temporalId,Optional.empty(),Optional.empty());
   var description=stub.describe();
   WorkflowExecutionStatus code=description.getStatus();
   temporalStatus=code.name();
   if(code==WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_RUNNING){
    try{state=temporal.newWorkflowStub(ExecutionWorkflow.class,r.temporalId).status();}catch(Exception ignored){state="RUNNING";}
   }else if(code==WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_COMPLETED)state="COMPLETED";
   else if(code==WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_FAILED)state="FAILED";
   else if(code==WorkflowExecutionStatus.WORKFLOW_EXECUTION_STATUS_CANCELED)state="CANCELLED";
  }catch(Exception ignored){}
  return Map.of("id",r.id,"workflowId",r.workflowId,"status",state,"temporalStatus",temporalStatus);
 }
 public List<StepEntity> steps(UUID id){run(id);return steps.findByIdRunIdOrderByIdNodeId(id);}
 public void approve(UUID id,String nodeId,boolean approved){
  var r=run(id);
  if(!"WAITING_APPROVAL".equals(status(id).get("status")))throw new ResponseStatusException(HttpStatus.CONFLICT,"Run is not awaiting approval");
  boolean pending=steps.findByIdRunIdOrderByIdNodeId(id).stream().anyMatch(s->s.id.nodeId.equals(nodeId)&&"WAITING_APPROVAL".equals(s.status));
  if(!pending)throw new ResponseStatusException(HttpStatus.CONFLICT,"This approval is not pending");
  temporal.newWorkflowStub(ExecutionWorkflow.class,r.temporalId).approve(nodeId,approved);
 }
 private String sha256(String data){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
