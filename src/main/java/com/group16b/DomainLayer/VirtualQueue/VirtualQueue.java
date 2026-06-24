package com.group16b.DomainLayer.VirtualQueue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "virtual_queues")
public class VirtualQueue {

    @Id
    private int id; 

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "virtual_queue_lines", joinColumns = @JoinColumn(name = "queue_id"))
    @OrderColumn(name = "queue_position") 
    @Column(name = "subject_id")
    private List<String> queueLine = new LinkedList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "virtual_queue_passed", joinColumns = @JoinColumn(name = "queue_id"))
    @MapKeyColumn(name = "subject_id")
    @Column(name = "passed_time")
    private Map<String, Long> passedQueue = new LinkedHashMap<>();

    private int pass_num; 
    
    private static final Integer PASS_NUM = 50;
    private static int PASS_TIMEOUT = 60 * 10 * 1000;

    // add a package-private setter specifically for tests that dont necessairly use dbms
    void setPassTimeoutForTest(int timeout) {
        PASS_TIMEOUT = timeout;
    }
    public static final int PASSED_QUEUE = -1;

    protected VirtualQueue() {}

    public VirtualQueue(int id) {
        this(id, PASS_NUM);
    }

    public VirtualQueue(int id, int pass_num) {
        this.id = id;
        this.pass_num = pass_num;
    }

    public VirtualQueue(VirtualQueue other) {
        this.queueLine = new LinkedList<>(other.queueLine);
        this.id = other.id;
        this.passedQueue = new LinkedHashMap<>(other.passedQueue);
        this.pass_num = other.pass_num;
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
        
        long currentTime = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> iterator = passedQueue.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if ((currentTime - entry.getValue()) > PASS_TIMEOUT) {
                iterator.remove();
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

	public int getQueuePosition(String subjectID) {
		int index = queueLine.indexOf(subjectID);
		if (index == -1)
			throw new IllegalArgumentException("Subject " + subjectID + " is not in the queue.");
		return index;
	}

	public int getQueueStatus(String subjectID) {
		addToQueue(subjectID);
		if (isUserPassedQueue(subjectID))
			return PASSED_QUEUE;
		return getQueuePosition(subjectID);
	}
}