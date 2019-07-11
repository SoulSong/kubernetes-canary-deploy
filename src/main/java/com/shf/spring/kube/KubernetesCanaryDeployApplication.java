package com.shf.spring.kube;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class KubernetesCanaryDeployApplication {

    @Value("${app.version:0.0.1}")
    private String version;

    public static void main(String[] args) {
        SpringApplication.run(KubernetesCanaryDeployApplication.class, args);
    }

    @GetMapping(value = "/get/app/version", produces = MediaType.TEXT_HTML_VALUE)
    public String getCurrentVersion() {
        return "Current version is : " + version;
    }

}
