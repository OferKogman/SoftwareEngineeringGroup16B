package com.group16b.InfrastructureLayer.MapDBs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.ProductionCompany.ProductionCompany;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;

public class VirtualQueueRepositoryMapImpl implements IRepository<VirtualQueue> {
	private final ConcurrentHashMap<Integer, VirtualQueue> queues = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Integer, ReentrantLock> locks = new ConcurrentHashMap<>();

	public VirtualQueueRepositoryMapImpl() {
	}

	@Override
	public VirtualQueue findByID(String ID) {
		ReentrantLock lock = locks.get(parseID(ID));
		if (lock == null) {
			throw new IllegalArgumentException("Virtual Queue does not exist");
		}
		lock.lock();
		VirtualQueue dbVirtualQueue = queues.get(parseID(ID));
		if (dbVirtualQueue == null) {
			lock.unlock();
			throw new IllegalArgumentException("Virtual Queue does not exist");
		}
		return new VirtualQueue(dbVirtualQueue);
	}


	@Override
	public List<VirtualQueue> getAll() {
		throw new UnsupportedOperationException("Virtual queue does not support 'getAll' operation");
	}


	@Override
	public void delete(String ID) {
		int id=parseID(ID);
		ReentrantLock lock = locks.remove(id);
		lock.lock();
		queues.remove(id);
		lock.unlock();
	}


	@Override
	public void save(VirtualQueue q) {
		if (!queues.contains(q.getId())) {
			locks.put(q.getId(), new ReentrantLock());
		}
		queues.put(q.getId(), q);
	}

	private int parseID(String ID)
    {
        try {
            return Integer.parseInt(ID);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid company ID: " + ID);
        }
    }
}