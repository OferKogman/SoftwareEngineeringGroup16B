package com.group16b.InfrastructureLayer.IdGenerators;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class ProductionCompanyIdGen {
    private final AtomicInteger currentId=new AtomicInteger(0);
    
    public void seed(int seed) {
        currentId.set(seed);
    }

    public int getNextId() {
        return currentId.incrementAndGet();
    }
}
