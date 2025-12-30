package de.angr2301.genericllmadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot Application for Generic LLM Adapter
 */
@SpringBootApplication
@EnableFeignClients(basePackages = "de.angr2301.genericllmadapter")
@EnableJpaRepositories(
    basePackages = {
        "de.angr2301.genericllmadapter.domain.user",
        "de.angr2301.genericllmadapter.domain.chat",
        "de.angr2301.genericllmadapter.repository"
    }
)
public class GenericLLMAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenericLLMAdapterApplication.class, args);
    }
}
