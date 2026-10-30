package com.agentflow.temporal;
import com.agentflow.domain.WorkflowSpec;
import io.temporal.workflow.*;
import java.util.Map;
@WorkflowInterface public interface ExecutionWorkflow {
 @WorkflowMethod Map<String,String> execute(WorkflowSpec spec);
 @SignalMethod void approve(String nodeId, boolean approved);
 @QueryMethod String status();
}
