package com.agentflow.domain;

import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class DagValidator {

  public void validate(WorkflowSpec spec) {
    if (
      spec.name() == null || spec.name().isBlank()
    ) throw new IllegalArgumentException("Workflow name is required");
    if (
      spec.nodes().isEmpty() || spec.nodes().size() > 100
    ) throw new IllegalArgumentException("Must contain 1-100 nodes");
    Map<String, WorkflowSpec.Node> nodes = new HashMap<>();
    for (var n : spec.nodes()) {
      if (
        n.id() == null || !n.id().matches("[a-zA-Z][a-zA-Z0-9_-]{0,63}")
      ) throw new IllegalArgumentException("Invalid node id");
      if (n.type() == null) throw new IllegalArgumentException(
        "Node type required"
      );
      if (
        n.type() == WorkflowSpec.NodeType.APPROVAL && n.dependsOn().isEmpty()
      ) throw new IllegalArgumentException(
        "Approval must depend on an earlier node"
      );
      if (
        nodes.putIfAbsent(n.id(), n) != null
      ) throw new IllegalArgumentException("Duplicate node: " + n.id());
    }
    Map<String, Integer> state = new HashMap<>();
    for (String id : nodes.keySet()) visit(id, nodes, state);
  }

  private void visit(
    String id,
    Map<String, WorkflowSpec.Node> nodes,
    Map<String, Integer> state
  ) {
    int s = state.getOrDefault(id, 0);
    if (s == 1) throw new IllegalArgumentException(
      "Workflow contains cycle at " + id
    );
    if (s == 2) return;
    state.put(id, 1);
    for (String dep : nodes.get(id).dependsOn()) {
      if (!nodes.containsKey(dep)) throw new IllegalArgumentException(
        "Missing dependency " + dep
      );
      if (dep.equals(id)) throw new IllegalArgumentException(
        "Self dependency: " + id
      );
      visit(dep, nodes, state);
    }
    state.put(id, 2);
  }
}
