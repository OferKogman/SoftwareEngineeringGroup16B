package com.group16b.DomainLayer.VirtualQueue;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class VirtualQueue {
	private final List<String> queueLine;
	private final Map<String, Long> passedQueue;
	private final int id;
	private final int pass_num;
	private static final Integer PASS_NUM=50;
	private final Integer PASS_TIMEOUT = 60 * 10 * 1000;

	public static final int PASSED_QUEUE = -1;

	public VirtualQueue(int id) {
		this(id,PASS_NUM);
	}
	public VirtualQueue(int id, int pass_num) {
		queueLine = new LinkedList<>();
		this.id = id;
		this.pass_num=pass_num;
		this.passedQueue = new LinkedHashMap<>();
	}

	public VirtualQueue(VirtualQueue other) {
		this.queueLine = new LinkedList<>(other.queueLine);
		this.id = other.id;
		this.passedQueue = new LinkedHashMap<>(other.passedQueue);
		this.pass_num=other.pass_num;
	}

	public void addToQueue(String subjectID) {
		popFirstIn();
		if (queueLine.contains(subjectID) || passedQueue.containsKey(subjectID))
			return;
		queueLine.add(subjectID);
		popFirstIn();
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
		while (passedQueue.size() < pass_num && !queueLine.isEmpty()) {
			passedQueue.put(queueLine.remove(0), System.currentTimeMillis());
		}
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

	public int getQueuePosition(String subjectID)
	{
		int index= queueLine.indexOf(subjectID);
		if(index==-1)
			throw new IllegalArgumentException("Subject "+subjectID+" is not in the queue.");
		return index;
	}

	public int getQueueStatus(String subjectID)
	{
		if(isUserPassedQueue(subjectID))
			return PASSED_QUEUE;
		return getQueuePosition(subjectID);
	}
}