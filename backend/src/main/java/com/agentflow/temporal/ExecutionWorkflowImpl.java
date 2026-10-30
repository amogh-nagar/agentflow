package com.agentflow.temporal;

import com.agentflow.domain.WorkflowSpec;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.*;
import java.time.Duration;
import java.util.*;

public class ExecutionWorkflowImpl implements ExecutionWorkflow {

  private final Map<String, Boolean> approvals = new HashMap<>();
  private String state = "RUNNING";
  private String pendingApproval = null;
  private final NodeActivities activities = Workflow.newActivityStub(
    NodeActivities.class,
    ActivityOptions.newBuilder()
      .setStartToCloseTimeout(Duration.ofSeconds(120))
      .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
      .build()
  );

  @Override
  public Map<String, String> execute(WorkflowSpec spec) {
    Map<String, String> outputs = new HashMap<>();
    List<WorkflowSpec.Node> remaining = new ArrayList<>(spec.nodes());
    String runId = Workflow.getInfo().getWorkflowId().replace("agentflow-", "");
    while (!remaining.isEmpty()) {
      List<WorkflowSpec.Node> ready = remaining
        .stream()
        .filter(n -> outputs.keySet().containsAll(n.dependsOn()))
        .toList();
      if (ready.isEmpty()) throw new IllegalStateException("Invalid DAG");
      // Schedule every executable non-approval activity before blocking on results.
      Map<String, Promise<String>> pending = new LinkedHashMap<>();
      for (var node : ready) {
        if (node.type() != WorkflowSpec.NodeType.APPROVAL) {
          String upstream = node
            .dependsOn()
            .stream()
            .map(dep -> "[" + dep + "]\n" + outputs.get(dep))
            .collect(java.util.stream.Collectors.joining("\n\n"));
          pending.put(
            node.id(),
            Async.function(
              activities::execute,
              runId,
              node.id(),
              node.type().name(),
              node.value(),
              upstream
            )
          );
        }
      }
      for (var entry : pending.entrySet())
        outputs.put(entry.getKey(), entry.getValue().get());
      for (var node : ready) {
        if (node.type() == WorkflowSpec.NodeType.APPROVAL) {
          pendingApproval = node.id();
          state = "WAITING_APPROVAL";
          activities.markApproval(runId, node.id(), "WAITING_APPROVAL");
          boolean decided = Workflow.await(Duration.ofHours(24), () ->
            approvals.containsKey(node.id())
          );
          if (!decided) {
            activities.markApproval(runId, node.id(), "TIMED_OUT");
            state = "TIMED_OUT";
            throw io.temporal.failure.ApplicationFailure.newNonRetryableFailure(
              "Approval timeout",
              "APPROVAL_TIMEOUT"
            );
          }
          if (!approvals.get(node.id())) {
            activities.markApproval(runId, node.id(), "REJECTED");
            state = "REJECTED";
            throw io.temporal.failure.ApplicationFailure.newNonRetryableFailure(
              "Approval rejected",
              "APPROVAL_REJECTED"
            );
          }
          activities.markApproval(runId, node.id(), "COMPLETED");
          pendingApproval = null;
          state = "RUNNING";
          outputs.put(node.id(), "APPROVED");
        }
      }
      remaining.removeAll(ready);
    }
    state = "COMPLETED";
    return outputs;
  }

  @Override
  public void approve(String nodeId, boolean approved) {
    if (nodeId.equals(pendingApproval)) approvals.putIfAbsent(nodeId, approved);
  }

  @Override
  public String status() {
    return state;
  }
}
