package com.group16b.DomainLayer.VirtualQueue;

public interface IVirtualQueueRepository {
    public VirtualQueue findVirtualQueueById(int id);
    
    public void saveVirtualQueue(VirtualQueue virtualQueue);

    public void addVirtualQueue(VirtualQueue virtualQueue);
}
