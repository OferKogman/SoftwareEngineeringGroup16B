package com.group16b.InfrastructureLayer.Adapters;


import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.group16b.DomainLayer.Order.IOrderRepository;
import com.group16b.DomainLayer.Order.Order;
import com.group16b.InfrastructureLayer.Database.OrderRepository;

@Component
@Primary
public class OrderRepositoryAdapter implements IOrderRepository {

    private final OrderRepository springRepo;

    public OrderRepositoryAdapter(OrderRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public List<Order> getAll() {
        return springRepo.findAll();
    }

    @Override
    public Order findByID(String id) {
        return springRepo.findById(id).orElseThrow(() -> 
            new IllegalArgumentException("Order with ID " + id + " not found.")
        );
    }

    @Override
    public void save(Order order) {
        springRepo.save(order);
    }

    @Override
    public void delete(String id) {
        springRepo.deleteById(id);
    }

    @Override
    public List<Order> getByEventId(int eventId) {
        return springRepo.findByEventId(eventId);
    }

    @Override
    public List<Order> getBySubjectId(String subjectId) {
        return springRepo.findBySubjectID(subjectId);
    }

    @Override
    public List<Order> getCompletedByEventIdSeatIds(int eventId, String segmentId, List<String> seatIds) {
        return springRepo.getCompletedByEventIdSeatIds(eventId, segmentId, seatIds);
    }

    @Override
    public List<Order> getCompletedByEventIdField(int eventId, String segmentId) {
        return springRepo.getCompletedByEventIdField(eventId, segmentId);
    }

    @Override
    public Order findFirstByUserIdAndActiveTrue(String userId) {
        return springRepo.findFirstBySubjectIDAndActiveTrue(userId).orElse(null);
    }

    @Override
    public List<Order> findByUserIdAndActiveFalse(String userId) {
        return springRepo.findBySubjectIDAndActiveFalse(userId);
    }
}
