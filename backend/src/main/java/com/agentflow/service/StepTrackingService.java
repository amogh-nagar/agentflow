package com.agentflow.service;
import com.agentflow.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;
@Service public class StepTrackingService {
 private final StepRepository repo;
 public StepTrackingService(StepRepository repo){this.repo=repo;}
 @Transactional public void begin(UUID run,String node){
  var key=new StepEntity.Key(run,node);
  var item=repo.findById(key).orElseGet(()->new StepEntity(run,node));
  item.attempts++; item.status="RUNNING";item.startedAt=Instant.now();item.errorMessage=null;repo.save(item);
 }
 @Transactional public void finish(UUID run,String node,String output){
  var item=repo.findById(new StepEntity.Key(run,node)).orElseGet(()->new StepEntity(run,node));
  item.status="COMPLETED";item.outputPreview=output==null?null:output.substring(0,Math.min(500,output.length()));
  item.completedAt=Instant.now();repo.save(item);
 }
 @Transactional public void fail(UUID run,String node,Exception e){
  var item=repo.findById(new StepEntity.Key(run,node)).orElseGet(()->new StepEntity(run,node));
  item.status="FAILED";item.errorMessage=e.getMessage()==null?e.getClass().getSimpleName():e.getMessage().substring(0,Math.min(500,e.getMessage().length()));
  item.completedAt=Instant.now();repo.save(item);
 }
 @Transactional public void approval(UUID run,String node,String status){
  var item=repo.findById(new StepEntity.Key(run,node)).orElseGet(()->new StepEntity(run,node));
  item.status=status;if("WAITING_APPROVAL".equals(status))item.startedAt=Instant.now();else item.completedAt=Instant.now();repo.save(item);
 }
}
