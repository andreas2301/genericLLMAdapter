package de.angr2301.genericllmadapter.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "vllm-health", url = "${vllm.url:http://localhost:8000}")
public interface VllmHealthClient {

    @GetMapping("/v1/models")
    Object checkHealth();
}
