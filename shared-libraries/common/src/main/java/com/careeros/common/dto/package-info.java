/**
 * Cross-cutting, domain-agnostic Data Transfer Objects reused by every service.
 *
 * <p>Only DTOs with no business meaning belong here (e.g. {@link com.careeros.common.dto.IdResponse},
 * {@link com.careeros.common.dto.MessageResponse}). Domain-specific DTOs live in their owning
 * service's {@code dto} package. Entities are never exposed directly — controllers always map to a
 * DTO.
 */
package com.careeros.common.dto;
