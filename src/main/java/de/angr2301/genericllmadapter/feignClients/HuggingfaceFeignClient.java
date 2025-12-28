package de.angr2301.genericllmadapter.feignClients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "huggingface", url = "https://api-inference.huggingface.co/models")
public interface HuggingfaceFeignClient {

        @PostMapping("/{modelId}")
        List<Map<String, Object>> generate(
                        @PathVariable("modelId") String modelId,
                        @RequestBody Map<String, Object> request);
}
