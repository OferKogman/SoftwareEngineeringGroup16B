package com.group16b.DomainLayer.Event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FieldSegTests {
	FieldSeg seg;

	@BeforeEach
	void setUp() {
		seg = new FieldSeg("F-1", 10);
		seg.addEvent(1);
	}

	@Test
	void SuccessfulRemoveStock() {
		seg.removeStock(1, 3);
		assertEquals(7, seg.getStock(1));
	}

	@Test
	void FailedRemoveStockInvalidEvent() {
		Exception ex = assertThrows(IllegalArgumentException.class, () -> {
			seg.removeStock(2, 5);
		});
		assertEquals("this event is not in this venue.", ex.getMessage());
	}

	@Test
	void FailedRemoveStockInvalidAmount() {
		Exception ex = assertThrows(IllegalArgumentException.class, () -> {
			seg.removeStock(1, 11);
		});
		assertEquals("Not enough tickets left in stock !", ex.getMessage());
	}

	@Test
	void SuccessfulAddStock() {
		seg.removeStock(1, 3);
		seg.addStock(1, 2);
		assertEquals(9, seg.getStock(1));
	}

	@Test
	void FailedAddStockInvalidEvent() {
		Exception ex = assertThrows(IllegalArgumentException.class, () -> {
			seg.addStock(2, 5);
		});
		assertEquals("this event is not in this venue.", ex.getMessage());
	}

	@Test
	void FailedAddStockInvalidAmount() {
		Exception ex = assertThrows(IllegalArgumentException.class, () -> {
			seg.addStock(1, 11);
		});
		assertEquals("Field is not big enough !", ex.getMessage());
	}
}
