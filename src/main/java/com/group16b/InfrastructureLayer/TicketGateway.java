package com.group16b.InfrastructureLayer;

import org.springframework.stereotype.Service;

import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Exceptions.TicketGenerationException;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;

@Service
public class TicketGateway implements ITicketGateway{

    @Override
    public TicketDTO generateTicket(int eventId, String subjectId, String segmentId, String seatId, double price) {
        if (eventId == -5){ // Simulate a failure in ticket generation for testing purposes
            throw new TicketGenerationException("Event ID cannot be null");
        }
        if(seatId == null) {
            seatId = "FIELD";
        }
        return new TicketDTO(String.valueOf(eventId), subjectId, segmentId, seatId, price);
    }

}
