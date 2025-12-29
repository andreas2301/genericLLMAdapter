package de.angr2301.genericllmadapter.domain.chat;

import de.angr2301.genericllmadapter.feign.VllmHealthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmHealthCheckService {

    private final VllmHealthClient vllmHealthClient;

    public boolean isVllmAlive() {
        try {
            vllmHealthClient.checkHealth();
            return true;
        } catch (Exception e) {
            log.trace("vLLM is not reachable: {}", e.getMessage());
            return false;
        }
    }
}
