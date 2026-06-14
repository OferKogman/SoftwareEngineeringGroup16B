package com.group16b.ApplicationLayer.Interfaces;

import java.util.List;

public interface ITicketGateway {
    //as the name suggests, generate the ticket for the seating segment including the seats
    String generateSeatingTicket(int eventId, String cusomerId, String zone, List<String> seats);
    //same but for standing area, includes the quantity
    String generateGeneralAdmissionTicket(int eventId, String cusomerId, String zone, int quantty);
    void revokeTicket(String externalTicketID);
}
