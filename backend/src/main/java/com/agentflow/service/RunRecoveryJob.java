package com.agentflow.service;
import com.agentflow.persistence.RunRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component public class RunRecoveryJob {
 private final RunRepository runs;private final WorkflowService service;
 public RunRecoveryJob(RunRepository runs,WorkflowService service){this.runs=runs;this.service=service;}
 @Scheduled(fixedDelayString="${agentflow.recovery-delay-ms:10000}")
 public void retryQueued(){for(var run:runs.findTop50ByStatusOrderByIdAsc("QUEUED"))service.dispatch(run);}
}
