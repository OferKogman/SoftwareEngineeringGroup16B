package com.group16b.DomainLayer.VirtualQueue;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VirtualQueue {
	private final List<String> queueLine;
	private final Map<String, Long> passedQueue;
	private long version;
	private final int id;
	private final int PASS_NUM = 50;
	private final int PASS_TIMEOUT = 60 * 10 * 1000;

	public VirtualQueue(int id) {
		queueLine = new LinkedList<>();
		this.version = 0;
		this.id = id;
		this.passedQueue = new LinkedHashMap<>();
	}

	public VirtualQueue(VirtualQueue other) {
		this.queueLine = new LinkedList<>(other.queueLine);
		this.version = other.version;
		this.id = other.id;
		this.passedQueue = new LinkedHashMap<>();
	}

	public synchronized boolean addToQueue(String subjectID) {
		popFirstIn();
		if (queueLine.contains(subjectID) || passedQueue.containsKey(subjectID))
			return false;
		queueLine.add(subjectID);
		this.version++;
		popFirstIn();
		return true;
	}

	private synchronized void popFirstIn() {
		if (queueLine.isEmpty()) {
			return;
		}
		for (Map.Entry<String, Long> entry : passedQueue.entrySet()) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - entry.getValue()) > PASS_TIMEOUT){
				passedQueue.remove(entry.getKey());
			} else {
				break;
			}
		}
		if (passedQueue.size() >= PASS_NUM) {
			return;
		}
		this.version++;
		passedQueue.put(queueLine.remove(0), System.currentTimeMillis());
	}

	public synchronized void removePassed(String subjectID) {
		this.version++;
		passedQueue.remove(subjectID);
	}

	public synchronized long getVersion() {
		return this.version;
	}

	public synchronized int getId() {
		return this.id;
	}

	public synchronized void setVersion(long version) {
		this.version = version;
	}

	public synchronized boolean isUserPassedQueue(String subjectID) throws NoSuchAlgorithmException {
		return passedQueue.containsKey(subjectID);
	}
}