package com.careeros.aiorchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI Orchestrator Service — the bridge between the Spring Boot ecosystem and the FastAPI AI service.
 *
 * <p><strong>Contains no AI logic and owns no database.</strong> Its sole responsibility is to be a
 * hardened, observable client in front of the Python AI service: forwarding authenticated requests,
 * validating payloads, applying timeouts and retries, and logging/tracing every downstream call.
 * Multiple HTTP client styles are wired (RestClient, WebClient, and an OpenFeign placeholder) so the
 * orchestration layer can choose the right tool per call once the AI endpoints are implemented.
 *
 * <p>Inherits the platform baseline from the shared libraries but excludes persistence — it is a
 * stateless service. This is an architectural skeleton: the actual FastAPI calls are not implemented.
 */
@SpringBootApplication(scanBasePackages = "com.careeros")
public class AiOrchestratorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiOrchestratorServiceApplication.class, args);
    }
}
