package com.agentflow.domain;
import java.util.*;
public record WorkflowSpec(String name, List<Node> nodes) {
 public WorkflowSpec {nodes = nodes == null ? List.of() : List.copyOf(nodes);}
 public record Node(String id, NodeType type, List<String> dependsOn, String value) {
  public Node { dependsOn = dependsOn == null ? List.of() : List.copyOf(dependsOn); }
 }
 public enum NodeType { CONSTANT, UPPERCASE, LLM, AGENT, FAIL_ONCE, APPROVAL }
}
