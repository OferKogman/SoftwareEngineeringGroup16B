package com.group16b.DomainLayer.VirtualQueue;

import java.util.Optional;

public interface IVirtualQueueRepository {
    public VirtualQueue findVirtualQueueById(long id);
    
    public void saveVirtualQueue(VirtualQueue virtualQueue);

    public void addVirtualQueue(VirtualQueue virtualQueue);
}