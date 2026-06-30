package com.careeros.aiorchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;

/**
 * Declarative HTTP client placeholder for the FastAPI AI service.
 *
 * <p>Intentionally declares no methods yet — it exists so the OpenFeign integration point is wired
 * and discoverable. The base URL is bound from {@code careeros.ai-service.base-url}. Endpoint methods
 * (e.g. resume review, interview generation) are added when the AI service contract is finalized.
 */
@FeignClient(name = "ai-service", url = "${careeros.ai-service.base-url}")
public interface AiServiceFeignClient {
}
