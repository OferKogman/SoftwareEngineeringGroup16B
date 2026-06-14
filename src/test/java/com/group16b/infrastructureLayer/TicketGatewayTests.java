package com.group16b.infrastructureLayer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.group16b.ApplicationLayer.Exceptions.IllegalTicketInfoException;
import com.group16b.ApplicationLayer.Exceptions.IssueTicketStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.RevokeTicketFailureException;
import com.group16b.ApplicationLayer.Exceptions.TicketGenerationException;
import com.group16b.ApplicationLayer.Exceptions.TicketRevokeUnknownStatusException;
import com.group16b.InfrastructureLayer.TicketGateway;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

public class TicketGatewayTests {
    private TicketGateway ticketGateway;
    private RestTemplate restTemplate;

    private final int VALID_EVENT_ID = 1;
    private final String VALID_CUSTOMER = "perry the platipus";
    private final String VALID_ZONE = "A";
    private List<String> VALID_SEATS;
    private final String INVALID_SEAT="hamburger.com";
    private final int VALID_QUANTITY = 2;

    private final String TICKET="ratatui is real and he is coming after you";

    @BeforeEach
    void setup()
    {
        restTemplate=mock(RestTemplate.class);
        ticketGateway=new TicketGateway(new WsepClient(restTemplate));
        VALID_SEATS = List.of("1-1","1-42");

        when(restTemplate.postForEntity(
        anyString(),
        any(HttpEntity.class),
        eq(String.class))).thenAnswer(invocation -> {
            HttpEntity<?> entity = invocation.getArgument(1);

            @SuppressWarnings("unchecked")
            MultiValueMap<String, String> body =(MultiValueMap<String, String>) entity.getBody();

            String action = body.getFirst("action_type");

            if ("issue_ticket".equals(action)) {
                return new ResponseEntity<>(TICKET, HttpStatus.OK);
            }

            if ("cancel_ticket".equals(action)) {
                return new ResponseEntity<>("1", HttpStatus.OK);
            }

            throw new IllegalStateException("Unexpected action: " + action);
        });
        
    }
    private List<String> validSeatsPlus(String seat) {
        List<String> seats = new ArrayList<>(VALID_SEATS);
        seats.add(seat);
        return seats;
    }

    private void mockCommunicationFailure() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("connection failed"));
    }

    @Test
    void givenValidTicketData_whenReturnedValidTicket_thenReturnCorrectTicket() {
        String ticketId = ticketGateway.generateSeatingTicket(VALID_EVENT_ID,VALID_CUSTOMER,VALID_ZONE,VALID_SEATS);

        assertEquals(TICKET, ticketId);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void seatingTicket_emptySeatList_throwsIllegalTicketInfoException(List<String> seats) {
        IllegalTicketInfoException ex = assertThrows(IllegalTicketInfoException.class,() -> ticketGateway.generateSeatingTicket(VALID_EVENT_ID,VALID_CUSTOMER,VALID_ZONE,seats));

        assertEquals("For a seating ticket, seats cannot be empty.", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void seatingTicket_emptySeat_throwsIllegalTicketInfoException(String seat) {
        List<String> seats = validSeatsPlus(seat);
        IllegalTicketInfoException ex = assertThrows(IllegalTicketInfoException.class,() -> ticketGateway.generateSeatingTicket(VALID_EVENT_ID,VALID_CUSTOMER,VALID_ZONE,seats));

        assertEquals("For a seating ticket, all seat identifiers must be non-blank.", ex.getMessage());
    }

    @Test
    void seatingTicket_IllegalSeatFormat_throwsIllegalTicketInfoException() {
        List<String> seats = validSeatsPlus(INVALID_SEAT);
        IllegalTicketInfoException ex = assertThrows(IllegalTicketInfoException.class,() -> ticketGateway.generateSeatingTicket(VALID_EVENT_ID,VALID_CUSTOMER,VALID_ZONE,seats));

        assertEquals("Invalid seat id format: "+INVALID_SEAT, ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0,-1,-100})
    void seatingTicket_invalidEventId_throwsIllegalTicketInfoException(int id) {
        IllegalTicketInfoException ex = assertThrows(IllegalTicketInfoException.class,() -> ticketGateway.generateSeatingTicket(id,VALID_CUSTOMER,VALID_ZONE,VALID_SEATS));

        assertEquals("Event id must be positive.", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void seatingTicket_blankUserId_throwsIllegalTicketInfoException(String moshe) {
        IllegalTicketInfoException ex = assertThrows(IllegalTicketInfoException.class,() -> ticketGateway.generateSeatingTicket(VALID_EVENT_ID,moshe,VALID_ZONE,VALID_SEATS));

        assertEquals("Customer id cannot be empty.", ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void seatingTicket_blankZone_throwsIllegalTicketInfoException(String zamzheno) {
        IllegalTicketInfoException ex = assertThrows(IllegalTicketInfoException.class,() -> ticketGateway.generateSeatingTicket(VALID_EVENT_ID,VALID_CUSTOMER,zamzheno,VALID_SEATS));

        assertEquals("Zone cannot be empty.", ex.getMessage());
    }

    @Test
    void IssueTicket_ticketIssueRefused_throwException()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>("-1", HttpStatus.OK));
        TicketGenerationException ex = assertThrows(TicketGenerationException.class,() -> ticketGateway.generateSeatingTicket( VALID_EVENT_ID, VALID_CUSTOMER, VALID_ZONE, VALID_SEATS));

        assertEquals("ticket provider refused to issue the ticket", ex.getMessage());
    }

    @Test
    void IssueTicket_restClientException_throwsIssueTicketUnknownStatus() {
        mockCommunicationFailure();

        IssueTicketStatusUnknownException ex = assertThrows(IssueTicketStatusUnknownException.class,() -> ticketGateway.generateSeatingTicket(VALID_EVENT_ID,VALID_CUSTOMER,VALID_ZONE,VALID_SEATS));

        assertEquals("Failed to contact ticket provider when issuing ticket.", ex.getMessage());
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "    "})
    void IssueTicket_nullOrBlankResponse_throwsIssueTicketUnknownStatus(String responseBody) {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        IssueTicketStatusUnknownException ex = assertThrows(IssueTicketStatusUnknownException.class,() -> ticketGateway.generateSeatingTicket(VALID_EVENT_ID,VALID_CUSTOMER,VALID_ZONE,VALID_SEATS));
        assertEquals("ticket provider returned empty response when issuing ticket.", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0,-1,-100})
    void standingTicket_invalidQuantity_throwsIllegalTicketInfoException(int num) {
        IllegalTicketInfoException ex = assertThrows(IllegalTicketInfoException.class,() -> ticketGateway.generateGeneralAdmissionTicket(VALID_EVENT_ID,VALID_CUSTOMER,VALID_ZONE,num));

        assertEquals("For a standing ticket, quantity must be positive.", ex.getMessage());
    }

    //--------------- REVOKE YOUR BELIEFS ---------------------

    @Test
    void revokeTicket_goodTicket_yay()
    {
        assertDoesNotThrow(()->ticketGateway.revokeTicket(TICKET));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void revokeTicket_givenBlankTicket_throwIllegaArg(String sasuke)
    {
        IllegalArgumentException ex=assertThrows(IllegalArgumentException.class, ()->ticketGateway.revokeTicket(sasuke));
        assertEquals("Ticket cannot be blank.", ex.getMessage());
    }

    @Test
    void revokeTicket_restClientException_throwsIssueTicketUnknownStatus() {
        mockCommunicationFailure();

        TicketRevokeUnknownStatusException ex = assertThrows(TicketRevokeUnknownStatusException.class,() -> ticketGateway.revokeTicket(TICKET));

        assertEquals("Failed to contact ticket provider when revoking ticket: "+TICKET, ex.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "    "})
    void revokeTicket_nullOrBlankResponse_throwsIssueTicketUnknownStatus(String responseBody) {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        TicketRevokeUnknownStatusException ex = assertThrows(TicketRevokeUnknownStatusException.class,() -> ticketGateway.revokeTicket(TICKET));
        assertEquals("ticket provider returned empty response when revoking ticket: "+TICKET, ex.getMessage());
    }

    @Test
    void revokeTicket_resultMinusOne_throwsRevokeFailureException() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("-1", HttpStatus.OK));

        RevokeTicketFailureException ex = assertThrows(RevokeTicketFailureException.class,() -> ticketGateway.revokeTicket(TICKET));

        assertTrue(ex.getMessage().contains("revoke failed for ticket"));
    }

    @Test
    void revokeTicket_invalidNumberResponse_throwsUnknownStatus() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("not-a-number", HttpStatus.OK));

        TicketRevokeUnknownStatusException ex = assertThrows(TicketRevokeUnknownStatusException.class,() -> ticketGateway.revokeTicket(TICKET));

        assertTrue(ex.getMessage().contains("Invalid response from ticket provider"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 999, -2})
    void revokeTicket_nonStandardResult_throwsUnknownStatus(int result) {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(String.valueOf(result), HttpStatus.OK));

        TicketRevokeUnknownStatusException ex = assertThrows(TicketRevokeUnknownStatusException.class,() -> ticketGateway.revokeTicket(TICKET));

        assertTrue(ex.getMessage().contains("invalid ticket revoke result"));
    }





    


    
}
