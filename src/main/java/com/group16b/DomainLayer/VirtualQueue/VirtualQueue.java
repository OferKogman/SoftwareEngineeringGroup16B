package com.group16b.DomainLayer.VirtualQueue;

import java.util.LinkedList;
import java.util.List;

public class VirtualQueue {
	private final List<String> queueLine;
	private long version;
	private final long id;

	protected VirtualQueue(long id) {
		queueLine = new LinkedList<>();
		this.version = 0;
		this.id = id;
	}

	protected VirtualQueue(VirtualQueue other) {
		this.queueLine = new LinkedList<>(other.queueLine);
		this.version = other.version;
		this.id = other.id;
	}

	protected synchronized boolean addToQueue(String userID) {
		if (queueLine.contains(userID))
			return false;
		queueLine.add(userID);
		this.version++;
		return true;
	}

	protected synchronized String popFirstIn() {
		if (queueLine.isEmpty())
			return null;
		this.version++;
		return queueLine.remove(0);
	}

	protected synchronized long getVersion() {
		return this.version;
	}

	protected synchronized long getId() {
		return this.id;
	}

	protected synchronized void setVersion(long version) {
		this.version = version;
	}
}
