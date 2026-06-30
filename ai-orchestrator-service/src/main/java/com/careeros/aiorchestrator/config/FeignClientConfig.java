package com.careeros.aiorchestrator.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Enables declarative OpenFeign HTTP clients, scanning {@code com.careeros.aiorchestrator.client}.
 *
 * <p>Provided as the third client style alongside {@code RestClient} and {@code WebClient}. The
 * placeholder {@code AiServiceFeignClient} is created but declares no endpoints yet — concrete
 * declarative methods are added once the FastAPI contract is finalized.
 */
@Configuration
@EnableFeignClients(basePackages = "com.careeros.aiorchestrator.client")
public class FeignClientConfig {
}
