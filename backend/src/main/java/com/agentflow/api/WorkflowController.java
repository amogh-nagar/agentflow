package com.agentflow.api;

import com.agentflow.domain.WorkflowSpec;
import com.agentflow.persistence.*;
import com.agentflow.service.WorkflowService;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class WorkflowController {

  private final WorkflowService service;

  public WorkflowController(WorkflowService service) {
    this.service = service;
  }

  @GetMapping("/workflows")
  public List<WorkflowEntity> workflows() {
    return service.list();
  }

  @PostMapping("/workflows")
  public ResponseEntity<WorkflowEntity> create(@RequestBody WorkflowSpec spec) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(spec));
  }

  @GetMapping("/workflows/{id}")
  public WorkflowEntity get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PostMapping("/workflows/{id}/runs")
  public ResponseEntity<RunEntity> start(
    @PathVariable UUID id,
    @RequestHeader("Idempotency-Key") String key
  ) {
    return ResponseEntity.accepted().body(service.start(id, key));
  }

  @GetMapping("/runs")
  public List<RunEntity> runs() {
    return service.listRuns();
  }

  @GetMapping("/runs/{id}")
  public Map<String, Object> status(@PathVariable UUID id) {
    return service.status(id);
  }

  @GetMapping("/runs/{id}/steps")
  public List<StepEntity> steps(@PathVariable UUID id) {
    return service.steps(id);
  }

  @GetMapping("/runs/{id}/steps/{nodeId}/output")
  public Map<String, String> fullOutput(
    @PathVariable UUID id,
    @PathVariable String nodeId
  ) {
    return Map.of("output", service.fullOutput(id, nodeId));
  }

  @PostMapping("/runs/{id}/approvals/{nodeId}")
  public ResponseEntity<Void> approve(
    @PathVariable UUID id,
    @PathVariable String nodeId,
    @RequestBody Approval request
  ) {
    service.approve(id, nodeId, request.approved());
    return ResponseEntity.noContent().build();
  }

  public record Approval(boolean approved) {}

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> invalid(
    IllegalArgumentException ex
  ) {
    return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
  }
}
