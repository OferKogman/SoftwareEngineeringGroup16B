package com.group16b.DomainLayer.VirtualQueue;

import java.util.concurrent.ConcurrentHashMap;

public class VirtualQueueRepository implements IVirtualQueueRepository{
	private final static VirtualQueueRepository instance = new VirtualQueueRepository();
	private final ConcurrentHashMap<Integer, VirtualQueue> queues = new ConcurrentHashMap<>();

	private VirtualQueueRepository() {
	}

	public static VirtualQueueRepository getInstance() {
		return instance;
	}

	@Override
	public VirtualQueue findVirtualQueueById(int id) {
		VirtualQueue dbVirtualQueue = queues.get(id);
		if (dbVirtualQueue == null) {
			throw new IllegalArgumentException("Queue does not exist");
		}

		return dbVirtualQueue;
	}


	@Override
	public synchronized void saveVirtualQueue(VirtualQueue virtualQueue) {
		VirtualQueue currQueue = findVirtualQueueById(virtualQueue.getId());
		VirtualQueue updatedQueue = new VirtualQueue(virtualQueue);
		updatedQueue.setVersion(virtualQueue.getVersion() + 1);

		boolean isReplaced = queues.replace(virtualQueue.getId(), currQueue, updatedQueue);
		if (!isReplaced) {
			throw new IllegalArgumentException(
					"Virtual Queue " + virtualQueue.getId()
							+ " version mismatch or concurrent modification. Expected verion " +
							virtualQueue.getVersion() + ", but found " + currQueue.getVersion() + " at read time");
		}
	}

	@Override
    public synchronized void addVirtualQueue(VirtualQueue virtualQueue){
		VirtualQueue currQueue = queues.get(virtualQueue.getId());
		if (currQueue != null) {
			throw new IllegalArgumentException("Virtual queue existing!");
		}

		queues.put(virtualQueue.getId(), virtualQueue);
    }
}