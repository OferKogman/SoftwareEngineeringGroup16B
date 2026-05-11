package com.group16b.DomainLayer.Order;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;


class OrderTests {

	

	@Test
	void activeOrderCompleteOrderReturnsCompletedOrder() {
		// Arrange
		ActiveOrder activeOrder = new ActiveOrder();

		// Act
		CompletedOrder completedOrder = activeOrder.completeOrder();

		// Assert
		assertNotNull(completedOrder);
		assertInstanceOf(CompletedOrder.class, completedOrder);
	}

	@Test
	void completedOrderCompleteOrderThrowsException() {
		// Arrange
		CompletedOrder completedOrder = new CompletedOrder();

		// Act + Assert
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				completedOrder::completeOrder);

		assertEquals("Order is already completed.", exception.getMessage());
	}

	@Test
	void seatOrderGetSeatsReturnsSeats() {
		// Arrange
		List<String> seats = List.of("A1", "A2", "A3");
		Order order = new Order("segment1", seats, 100.0, 1, "1");

		// Act
		List<String> actualSeats = order.getSeats();

		// Assert
		assertEquals(seats, actualSeats);
	}

	@Test
	void fieldOrderGetSeatsThrowsException() {
		// Arrange
		Order order = new Order("segment1", 3, 100.0, 1, "1");

		// Act + Assert
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				order::getSeats);

		assertEquals(
				"This order is for field tickets, it does not have specific seats.",
				exception.getMessage());
	}

	@Test
	void completeOrderChangesStateToCompletedOrder() {
		// Arrange
		Order order = new Order("segment1", List.of("A1", "A2"), 100.0, 1, "1");

		assertInstanceOf(ActiveOrder.class, order.getState());

		// Act
		order.CompleteOrder();

		// Assert
		assertInstanceOf(CompletedOrder.class, order.getState());
	}

	@Test
	void seatOrderNumOfTicketsEqualsNumberOfSeats() {
		// Arrange
		Order order = new Order("segment1", List.of("A1", "A2", "A3"), 100.0, 1, "1");

		// Act
		int numOfTickets = order.getNumOfTickets();

		// Assert
		assertEquals(3, numOfTickets);
	}

	@Test
	void fieldOrderNumOfTicketsEqualsRequestedAmount() {
		// Arrange
		Order order = new Order("segment1", 5, 100.0, 1, "1");

		// Act
		int numOfTickets = order.getNumOfTickets();

		// Assert
		assertEquals(5, numOfTickets);
	}

    @Test
    void seatOrderCreation_shouldInitializeBasicFields() {
        List<String> seats = List.of("A-1", "A-2");

        Order order = new Order("segment1", seats, 50.0, 7, "10");

        assertEquals(OrderType.SEAT, order.getOrderType());
        assertEquals("segment1", order.getSegmentId());
        assertEquals(2, order.getNumOfTickets());
        assertEquals(100.0, order.getTotalOrderprice());
        assertEquals(7, order.getEventId());
        assertEquals(seats, order.getSeats());
        assertTrue(order.isActive());
    }

    @Test
    void fieldOrderCreation_shouldInitializeBasicFields() {
        Order order = new Order("field1", 3, 40.0, 7, "10");

        assertEquals(OrderType.FIELD, order.getOrderType());
        assertEquals("field1", order.getSegmentId());
        assertEquals(3, order.getNumOfTickets());
        assertEquals(120.0, order.getTotalOrderprice());
        assertEquals(7, order.getEventId());
        assertTrue(order.isActive());
    }

    @Test
    void fieldOrderGetSeats_shouldThrowException() {
        Order order = new Order("field1", 3, 40.0, 7, "10");

        assertThrows(IllegalStateException.class, order::getSeats);
    }

    @Test
    void fieldOrderGetPricesPerSeat_shouldReturnEqualPrices() {
        Order order = new Order("field1", 3, 40.0, 7, "10");

        assertEquals(40.0, order.getPricesPerSeat());
    }

    @Test
    void seatOrderGetPricesPerSeat_shouldNotBeNull() {
        Order order = new Order("segment1", List.of("A-1", "A-2"), 50.0, 7, "10");

        assertNotNull(order.getPricesPerSeat());
        assertEquals(50.0, order.getPricesPerSeat());
    }

    @Test
    void completeOrder_shouldChangeStateToCompleted() {
        Order order = new Order("segment1", List.of("A-1"), 50.0, 7, "10");

        assertTrue(order.isActive());

        boolean result = order.CompleteOrder();

        assertTrue(result);
        assertFalse(order.isActive());
    }

    @Test
    void isBelongsToUser_sameToken_shouldReturnTrue() {
        Order order = new Order("segment1", List.of("A-1"), 50.0, 7, "10");

        assertTrue(order.isBelongsToSubject("10"));
    }

    @Test
    void isBelongsToUser_differentToken_shouldReturnFalse() {
        Order order = new Order("segment1", List.of("A-1"), 50.0, 7, "10");

        assertFalse(order.isBelongsToSubject("wrongSubject"));
    }
}