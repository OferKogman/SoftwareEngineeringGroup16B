package com.group16b.DomainLayer.VirtualQueue;

public interface IVirtualQueueRepository {
    public VirtualQueue findVirtualQueueById(long id);
    
    public void saveVirtualQueue(VirtualQueue virtualQueue);

    public void addVirtualQueue(VirtualQueue virtualQueue);
    public boolean isUserPassedQueue(int userId, int eventId);

}
