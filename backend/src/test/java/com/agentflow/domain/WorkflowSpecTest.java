package com.agentflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class WorkflowSpecTest {

  private final DagValidator validator = new DagValidator();

  @Test
  void validAiWorkflow() {
    assertDoesNotThrow(() ->
      validator.validate(
        new WorkflowSpec(
          "Review",
          List.of(
            new WorkflowSpec.Node(
              "input",
              WorkflowSpec.NodeType.CONSTANT,
              List.of(),
              "data"
            ),
            new WorkflowSpec.Node(
              "ai",
              WorkflowSpec.NodeType.LLM,
              List.of("input"),
              "summarize"
            ),
            new WorkflowSpec.Node(
              "approval",
              WorkflowSpec.NodeType.APPROVAL,
              List.of("ai"),
              null
            )
          )
        )
      )
    );
  }

  @Test
  void rejectsCycle() {
    assertThrows(IllegalArgumentException.class, () ->
      validator.validate(
        new WorkflowSpec(
          "cyclic",
          List.of(
            new WorkflowSpec.Node(
              "a",
              WorkflowSpec.NodeType.CONSTANT,
              List.of("b"),
              "x"
            ),
            new WorkflowSpec.Node(
              "b",
              WorkflowSpec.NodeType.UPPERCASE,
              List.of("a"),
              null
            )
          )
        )
      )
    );
  }
}
