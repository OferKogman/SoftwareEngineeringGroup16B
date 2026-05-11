package com.group16b.ApplicationLayer.Interfaces;

import com.group16b.ApplicationLayer.DTOs.TicketDTO;

public interface ITicketGateway {
    TicketDTO generateTicket(int eventId, String subjectID, String segmentId, String seatId, double price);
}
