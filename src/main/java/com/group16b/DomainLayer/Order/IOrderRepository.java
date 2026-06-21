package com.group16b.DomainLayer.Order;

import java.util.List;

import com.group16b.DomainLayer.Interfaces.IRepository;

public interface IOrderRepository extends IRepository<Order> {
    List<Order> getBySubjectId(String subjectId);
    List<Order> getCompletedByEventIdSeatIds(int eventId, String segmentId, List<String> seatIds);
    List<Order> getCompletedByEventIdField(int eventId, String segmentId);
    }
