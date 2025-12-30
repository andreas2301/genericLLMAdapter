package de.angr2301.genericllmadapter.feign;

import de.angr2301.genericllmadapter.dto.analysis.AnalysisRequest;
import de.angr2301.genericllmadapter.dto.analysis.AnalysisResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "analysisClient", url = "${analysis.service.url:http://localhost:8001}")
public interface AnalysisClient {

    @PostMapping("/analyze")
    AnalysisResponse analyze(@RequestBody AnalysisRequest request);
}
