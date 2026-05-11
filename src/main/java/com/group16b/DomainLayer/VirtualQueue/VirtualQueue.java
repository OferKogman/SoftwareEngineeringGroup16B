package com.group16b.DomainLayer.VirtualQueue;

import java.security.MessageDigest;
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

	protected VirtualQueue(VirtualQueue other) {
		this.queueLine = new LinkedList<>(other.queueLine);
		this.version = other.version;
		this.id = other.id;
		this.passedQueue = new LinkedHashMap<>();
	}

	public synchronized boolean addToQueue(String sessionToken) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(sessionToken.getBytes());
		String stringHash = new String(messageDigest.digest());
		if (queueLine.contains(stringHash))
			return false;
		queueLine.add(stringHash);
		this.version++;
		return true;
	}

	public synchronized void popFirstIn() {
		if (queueLine.isEmpty()) {
			throw new IllegalStateException("Queue is empty");
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

	public synchronized void removePassed(String sessionToken) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(sessionToken.getBytes());
		String stringHash = new String(messageDigest.digest());
		this.version++;
		passedQueue.remove(stringHash);
	}

	protected synchronized long getVersion() {
		return this.version;
	}

	protected synchronized int getId() {
		return this.id;
	}

	protected synchronized void setVersion(long version) {
		this.version = version;
	}

	public synchronized boolean isUserPassedQueue(String sessionToken) throws NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(sessionToken.getBytes());
		String stringHash = new String(messageDigest.digest());
		return passedQueue.containsKey(stringHash);
	}
}