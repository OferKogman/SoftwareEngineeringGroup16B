package com.group16b.InfrastructureLayer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonpCharacterEscapes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Exceptions.IllegalTicketInfoException;
import com.group16b.ApplicationLayer.Exceptions.IssueTicketStatusUnknownException;
import com.group16b.ApplicationLayer.Exceptions.RevokeTicketFailureException;
import com.group16b.ApplicationLayer.Exceptions.TicketGenerationException;
import com.group16b.ApplicationLayer.Exceptions.TicketRevokeUnknownStatusException;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

@Service
public class TicketGateway implements ITicketGateway{

    private final WsepClient wsepClient;
    private final ObjectMapper objectMapper=new ObjectMapper();

    public TicketGateway(WsepClient wsepClient)
    {
        this.wsepClient=wsepClient;
    }

    @Override
    public String generateSeatingTicket(int eventId, String cusomerId, String zone, List<String> seats)
    {
        validateSeatingArgs(eventId, cusomerId, zone, seats);
        MultiValueMap<String, String> requestBody=prepareTicketIssueBody(eventId, cusomerId, zone);
        requestBody.add("is_seating","true");

        List<Map<String,String>> seatPayload= seats.stream().map( id -> Map.of("seat", id)).toList();
        try{
            requestBody.add("seats", objectMapper.writeValueAsString(seatPayload));
        }
        catch(JsonProcessingException e){
            throw new IllegalTicketInfoException("Error mapping seats to payload: ",e);
        }

        String responseBody=wsepClient.sendRequest(requestBody,
                e-> new IssueTicketStatusUnknownException("Failed to contact ticket provider when issueing ticket."),
                ()-> new IssueTicketStatusUnknownException("ticket provider returned empty response when issuing ticket."));
        responseBody=responseBody.trim();
        if("-1".equals(responseBody))
            throw new TicketGenerationException("ticket provider refused to issue the ticket");

        return responseBody;
    }

    @Override
    public void revokeTicket(String externalTicketID)
    {
        if(externalTicketID==null || externalTicketID.isBlank())
            throw new IllegalArgumentException("Ticket cannot be blank.");
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("action_type","cancel_ticket");
        requestBody.add("ticket_id", externalTicketID);

        String responseBody=wsepClient.sendRequest(requestBody, 
                e -> new TicketRevokeUnknownStatusException("Failed to contact ticket provider when revoking ticket: "+externalTicketID, e), 
                () -> new TicketRevokeUnknownStatusException("ticket provider returned empty response when revoking ticket: "+externalTicketID));

        final int revokeResult=wsepClient.parseIntegerResponse(responseBody, body -> new TicketRevokeUnknownStatusException("Invalid response from ticket provider: " + body));
        
        wsepClient.validateSuccessFailureResult(revokeResult, 
            ()->new RevokeTicketFailureException("revoke failed for ticket: " + externalTicketID),
            ()-> new TicketRevokeUnknownStatusException("Provider returned invalid ticket revoke result: " + revokeResult + ", for ticket: " + externalTicketID));
    }


    //no idea what to validate about eventId so whoever knows the busniess rules add the validation later
    private void validateSeatingArgs(int eventId, String customer, String zone, List<String> seats)
    {
        if(seats==null || seats.isEmpty())
            throw new IllegalTicketInfoException("For a seating ticket, seats cannot be empty.");
        validatCommonTicketArgs(eventId, customer, zone);
    }

    private void validateStandingArgs(int eventId, String customer, String zone, int quantity)
    {
        if(quantity<=0)
            throw new IllegalTicketInfoException("For a standing ticket, quanty must be positive.");
        validatCommonTicketArgs(eventId, customer, zone);
    }

    private void validatCommonTicketArgs(int eventId, String customer, String zone)
    {
        if(customer==null || customer.isBlank())
            throw new IllegalTicketInfoException("Customer id cannot be empty.");
        if(zone==null || zone.isBlank())
            throw new IllegalTicketInfoException("Zone cannot be empty.");
    }

    private MultiValueMap<String, String> prepareTicketIssueBody(int eventId, String customer, String zone)
    {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("action_type","issue_ticket");
        requestBody.add("event_id", String.valueOf(eventId));
        requestBody.add("customer_id", customer);
        requestBody.add("zone", zone);

        return requestBody;
    }

}
