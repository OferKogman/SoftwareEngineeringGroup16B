package com.group16b.DomainLayer.Order;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class OrderTest {

	@Test
	void activeOrderGetTicketsThrowsException() {
		// Arrange
		ActiveOrder activeOrder = new ActiveOrder();

		// Act + Assert
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				activeOrder::getTickets);

		assertEquals("Cannot get tickets from an active order", exception.getMessage());
	}

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
		CompletedOrder completedOrder = new CompletedOrder(List.of("ticket1", "ticket2"));

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
		Order order = new Order("segment1", seats, "sTocken");

		// Act
		List<String> actualSeats = order.getSeats();

		// Assert
		assertEquals(seats, actualSeats);
	}

	@Test
	void fieldOrderGetSeatsThrowsException() {
		// Arrange
		Order order = new Order("segment1", 3, "sTocken");

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
		Order order = new Order("segment1", List.of("A1", "A2"), "sTocken");

		assertInstanceOf(ActiveOrder.class, order.getState());

		// Act
		order.CompleteOrder();

		// Assert
		assertInstanceOf(CompletedOrder.class, order.getState());
	}

	@Test
	void seatOrderNumOfTicketsEqualsNumberOfSeats() {
		// Arrange
		Order order = new Order("segment1", List.of("A1", "A2", "A3"), "sTocken");

		// Act
		int numOfTickets = order.getNumOfTickets();

		// Assert
		assertEquals(3, numOfTickets);
	}

	@Test
	void fieldOrderNumOfTicketsEqualsRequestedAmount() {
		// Arrange
		Order order = new Order("segment1", 5, "sTocken");

		// Act
		int numOfTickets = order.getNumOfTickets();

		// Assert
		assertEquals(5, numOfTickets);
	}
}