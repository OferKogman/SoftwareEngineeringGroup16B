package com.group16b.InfrastructureLayer;

import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;

public class TicketGateway implements ITicketGateway{

    @Override
    public TicketDTO generateTicket(int eventId, String subjectId, String segmentId, String seatId, double price) {
        return null;
    }

}
