package com.agentflow.domain;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class MultiAgentDagTest {
 private final DagValidator validator=new DagValidator();
 @Test void acceptsParallelAgentsAndSynthesis(){
  var spec=new WorkflowSpec("research", List.of(
   new WorkflowSpec.Node("topic", WorkflowSpec.NodeType.CONSTANT,List.of(),"Topic"),
   new WorkflowSpec.Node("researcher",WorkflowSpec.NodeType.AGENT,List.of("topic"),"Research"),
   new WorkflowSpec.Node("critic",WorkflowSpec.NodeType.AGENT,List.of("topic"),"Critique"),
   new WorkflowSpec.Node("synthesis",WorkflowSpec.NodeType.LLM,List.of("researcher","critic"),"Synthesize")));
  assertDoesNotThrow(()->validator.validate(spec));
 }
 @Test void rejectsUnknownDependency(){
  var spec=new WorkflowSpec("broken",List.of(new WorkflowSpec.Node("a",WorkflowSpec.NodeType.AGENT,List.of("missing"),"")));
  assertThrows(IllegalArgumentException.class,()->validator.validate(spec));
 }
}
