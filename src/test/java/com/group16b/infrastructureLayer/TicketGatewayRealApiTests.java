package com.group16b.infrastructureLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.group16b.InfrastructureLayer.TicketGateway;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

@Tag("external")
class TicketGatewayRealApiTests {

    private final TicketGateway ticketGateway =
            new TicketGateway(new WsepClient(new RestTemplate()));

    @Test
    void generateSeatingTicket_returnsTicketId() {
        String ticketId = ticketGateway.generateSeatingTicket(
                123,
                "smoke-test-user",
                "A",
                List.of("A-1", "A-2"));

        assertNotNull(ticketId);
        assertFalse(ticketId.isBlank());
    }

    @Test
    void generateGeneralAdmissionTicket_returnsTicketId() {
        String ticketId = ticketGateway.generateGeneralAdmissionTicket(
                123,
                "smoke-test-user",
                "A",
                2);

        assertNotNull(ticketId);
        assertFalse(ticketId.isBlank());
    }

    @Test
    void seatingTicket_canBeIssuedAndRevoked() {
        String ticketId = ticketGateway.generateSeatingTicket(
                123,
                "smoke-test-user",
                "A",
                List.of("A-1", "A-2"));

        assertDoesNotThrow(() ->
                ticketGateway.revokeTicket(ticketId));
    }

    @Test
    void generalAdmissionTicket_canBeIssuedAndRevoked() {
        String ticketId = ticketGateway.generateGeneralAdmissionTicket(
                123,
                "smoke-test-user",
                "A",
                2);

        assertDoesNotThrow(() ->
                ticketGateway.revokeTicket(ticketId));
    }
}