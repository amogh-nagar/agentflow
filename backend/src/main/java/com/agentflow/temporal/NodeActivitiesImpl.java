package com.agentflow.temporal;
import com.agentflow.service.*;
import org.springframework.stereotype.Component;
import java.util.*;
@Component public class NodeActivitiesImpl implements NodeActivities {
 private final StepTrackingService steps;private final AiGateway ai;
 public NodeActivitiesImpl(StepTrackingService steps,AiGateway ai){this.steps=steps;this.ai=ai;}
 @Override public String execute(String runId,String nodeId,String type,String value,String upstream){
  UUID run=UUID.fromString(runId);steps.begin(run,nodeId);
  try{
   String result=switch(type){
    case "CONSTANT" -> value==null?"":value;
    case "UPPERCASE" -> (upstream==null?"":upstream).toUpperCase(Locale.ROOT);
    case "LLM" -> ai.complete((value==null?"":value)+"\n"+(upstream==null?"":upstream));
    default -> throw new IllegalArgumentException("Unsupported activity type: "+type);
   };
   steps.finish(run,nodeId,result);return result;
  }catch(RuntimeException e){steps.fail(run,nodeId,e);throw e;}
 }
 @Override public void markApproval(String runId,String nodeId,String state){steps.approval(UUID.fromString(runId),nodeId,state);}
}
