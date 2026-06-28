// package com.group16b.infrastructureLayer.Database;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import com.group16b.ApiLayer.SoftwareEngineeringGroup16BApplication;
// import com.group16b.DomainLayer.Order.Order;
// import com.group16b.InfrastructureLayer.Database.OrderRepository;

// @SpringBootTest(
//     classes = SoftwareEngineeringGroup16BApplication.class,
//     properties = {
//         "spring.datasource.url=jdbc:postgresql://localhost:5433/postgres?sslmode=disable",
//         "spring.datasource.username=postgres",
//         "spring.datasource.password=h?Nkb*G=[MT]T).f"
//     }
// )
// public class OrderRepositoryIntegrationTest {

//     @Autowired
//     private OrderRepository orderRepository;

//     @Test
//     void saveOrder_shouldPersistToDatabase() {
//         Order order = new Order("segment1", List.of("A1", "A2"), 100.0, 1, "user1");
//         orderRepository.save(order);

//         Order found = orderRepository.findById(order.getOrderId()).orElse(null);
//         assertNotNull(found);
//         assertEquals("segment1", found.getSegmentId());
//     }
// }