package com.agentflow.domain;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class DagValidatorTest {
 private final DagValidator validator=new DagValidator();
 @Test void acceptsValidDependencies(){assertDoesNotThrow(()->validator.validate(new WorkflowSpec("test",List.of(new WorkflowSpec.Node("a",WorkflowSpec.NodeType.CONSTANT,List.of(),"ok"),new WorkflowSpec.Node("b",WorkflowSpec.NodeType.UPPERCASE,List.of("a"),null)))));}
 @Test void rejectsCycles(){assertThrows(IllegalArgumentException.class,()->validator.validate(new WorkflowSpec("cycle",List.of(new WorkflowSpec.Node("a",WorkflowSpec.NodeType.CONSTANT,List.of("b"),"a"),new WorkflowSpec.Node("b",WorkflowSpec.NodeType.CONSTANT,List.of("a"),"b")))));}
 @Test void rejectsUnknownDependencies(){assertThrows(IllegalArgumentException.class,()->validator.validate(new WorkflowSpec("bad",List.of(new WorkflowSpec.Node("a",WorkflowSpec.NodeType.CONSTANT,List.of("missing"),"")))));}
}
