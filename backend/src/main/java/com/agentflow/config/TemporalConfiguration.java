package com.agentflow.config;

import com.agentflow.temporal.*;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.WorkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class TemporalConfiguration {

  @Bean(destroyMethod = "shutdown")
  WorkflowServiceStubs temporalService(
    @Value("${agentflow.temporal.address}") String address
  ) {
    return WorkflowServiceStubs.newServiceStubs(
      WorkflowServiceStubsOptions.newBuilder().setTarget(address).build()
    );
  }

  @Bean
  WorkflowClient temporalClient(WorkflowServiceStubs svc) {
    return WorkflowClient.newInstance(svc);
  }

  @Bean(initMethod = "start", destroyMethod = "shutdown")
  WorkerFactory workerFactory(
    WorkflowClient client,
    NodeActivitiesImpl activities,
    @Value("${agentflow.temporal.task-queue}") String queue
  ) {
    WorkerFactory f = WorkerFactory.newInstance(client);
    var worker = f.newWorker(queue);
    worker.registerWorkflowImplementationTypes(ExecutionWorkflowImpl.class);
    worker.registerActivitiesImplementations(activities);
    return f;
  }
}
