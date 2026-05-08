package com.group16b.DomainLayer.VirtualQueue;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class IVirtualQueueRepository {
	private final ConcurrentHashMap<Long, VirtualQueue> queues = new ConcurrentHashMap<>();

	public Optional<VirtualQueue> findById(long id) {
		VirtualQueue dbVirtualQueue = queues.get(id);
		if (dbVirtualQueue != null) {
			return Optional.of(new VirtualQueue(dbVirtualQueue));
		}

		return Optional.empty();
	}

	public synchronized void save(VirtualQueue virtualQueue) {
		VirtualQueue currQueue = queues.get(virtualQueue.getId());
		if (currQueue == null) {
			throw new IllegalArgumentException("Virtual queue not found fr update!");
		}
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
}
