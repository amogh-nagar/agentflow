package com.agentflow.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
@Component public class AiGateway {
 private final ObjectMapper mapper;private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
 @Value("${agentflow.ai.mode:mock}") private String mode;
 @Value("${agentflow.ai.api-key:}") private String key;
 @Value("${agentflow.ai.endpoint:https://api.openai.com/v1/chat/completions}") private String endpoint;
 @Value("${agentflow.ai.model:gpt-4o-mini}") private String model;
 public AiGateway(ObjectMapper mapper){this.mapper=mapper;}
 public String complete(String prompt){
  if("mock".equalsIgnoreCase(mode))return "[DEMO AI RESPONSE] "+prompt.substring(0,Math.min(prompt.length(),200));
  if(!"openai".equalsIgnoreCase(mode))throw new IllegalArgumentException("Unknown AI mode");
  if(key.isBlank())throw new IllegalStateException("AI_API_KEY is required for openai mode");
  if(!endpoint.startsWith("https://"))throw new IllegalArgumentException("AI endpoint must use HTTPS");
  try{
   String body=mapper.writeValueAsString(Map.of("model",model,"messages",java.util.List.of(Map.of("role","user","content",prompt)),"max_tokens",300));
   var request=HttpRequest.newBuilder(URI.create(endpoint)).timeout(Duration.ofSeconds(25))
    .header("Authorization","Bearer "+key).header("Content-Type","application/json")
    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
   var response=client.send(request,HttpResponse.BodyHandlers.ofString());
   if(response.statusCode()/100!=2)throw new IllegalStateException("AI request failed with HTTP "+response.statusCode());
   JsonNode json=mapper.readTree(response.body());
   String content=json.path("choices").path(0).path("message").path("content").asText("");
   if(content.isBlank())throw new IllegalStateException("AI provider returned empty content");
   return content;
  }catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("AI request interrupted",e);
  }catch(java.io.IOException e){throw new IllegalStateException("AI provider unavailable",e);}
 }
}
