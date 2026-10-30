package com.agentflow.temporal;

import io.temporal.activity.*;

@ActivityInterface
public interface NodeActivities {
  @ActivityMethod
  String execute(
    String runId,
    String nodeId,
    String type,
    String value,
    String upstream
  );

  @ActivityMethod
  void markApproval(String runId, String nodeId, String state);
}
