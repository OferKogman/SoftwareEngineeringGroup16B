package com.group16b.DomainLayer.VirtualQueue;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VirtualQueue {
	private final List<String> queueLine;
	private final Map<String, Long> passedQueue;
	private final int id;
	private final int PASS_NUM = 50;
	private final int PASS_TIMEOUT = 60 * 10 * 1000;

	public VirtualQueue(int id) {
		queueLine = new LinkedList<>();
		this.id = id;
		this.passedQueue = new LinkedHashMap<>();
	}

	public VirtualQueue(VirtualQueue other) {
		this.queueLine = new LinkedList<>(other.queueLine);
		this.id = other.id;
		this.passedQueue = new LinkedHashMap<>(other.passedQueue);
	}

	public boolean addToQueue(String subjectID) {
		popFirstIn();
		if (queueLine.contains(subjectID) || passedQueue.containsKey(subjectID))
			return false;
		queueLine.add(subjectID);
		popFirstIn();
		return true;
	}

	private void popFirstIn() {
		if (queueLine.isEmpty()) {
			return;
		}
		for (Map.Entry<String, Long> entry : passedQueue.entrySet()) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - entry.getValue()) > PASS_TIMEOUT) {
				passedQueue.remove(entry.getKey());
			} else {
				break;
			}
		}
		if (passedQueue.size() >= PASS_NUM) {
			return;
		}
		passedQueue.put(queueLine.remove(0), System.currentTimeMillis());
	}

	public void removePassed(String subjectID) {
		passedQueue.remove(subjectID);
	}

	public int getId() {
		return this.id;
	}

	public boolean isUserPassedQueue(String subjectID) {
		return passedQueue.containsKey(subjectID);
	}

	public void validateUserPassedQueue(String subjectID) {
		if (!passedQueue.containsKey(subjectID)) {
			throw new IllegalStateException("User did not pass the queue.");
		}
	}
}