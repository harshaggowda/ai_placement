/**
 * CareerOS AI — ai-orchestrator-service.
 *
 * <p>Bridge between the Spring ecosystem and the FastAPI AI service. Contains no AI logic and owns
 * no database; responsible for calling FastAPI with authentication forwarding, request validation,
 * retries, timeouts, and logging.
 *
 * <p>{@code config} holds the downstream client wiring (RestClient, WebClient, retry, and an
 * OpenFeign placeholder); {@code controller}, {@code service}, {@code dto}, and {@code util} are
 * reserved for the request-forwarding layer (not implemented).
 */
package com.careeros.aiorchestrator;
