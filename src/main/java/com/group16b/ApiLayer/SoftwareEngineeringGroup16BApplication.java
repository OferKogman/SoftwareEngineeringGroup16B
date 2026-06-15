package com.group16b.ApiLayer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.group16b")
@EntityScan(basePackages = "com.group16b.DomainLayer")
@EnableJpaRepositories(basePackages = "com.group16b") 
public class SoftwareEngineeringGroup16BApplication {

    public static void main(String[] args) {
        SpringApplication.run(SoftwareEngineeringGroup16BApplication.class, args);
    }
}