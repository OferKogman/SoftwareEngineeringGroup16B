package com.group16b.ApplicationLayer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartUpRunner implements ApplicationRunner {
    private final StartupService startupService;

    public StartUpRunner(StartupService startupService) {
        this.startupService = startupService;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        startupService.initializeSystem();
    }
    
}
