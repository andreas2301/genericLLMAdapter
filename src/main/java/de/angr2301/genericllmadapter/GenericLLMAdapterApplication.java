package de.angr2301.genericllmadapter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "de.angr2301.genericllmadapter")
public class GenericLLMAdapterApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenericLLMAdapterApplication.class, args);
    }
}
