package com.group16b.InfrastructureLayer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.group16b.ApplicationLayer.DTOs.TicketDTO;
import com.group16b.ApplicationLayer.Exceptions.RefundFailedException;
import com.group16b.ApplicationLayer.Exceptions.TicketGenerationException;
import com.group16b.ApplicationLayer.Exceptions.TicketRevokeUnknownStatusException;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;

@Service
public class TicketGateway implements ITicketGateway{

    private static final String BASE_URL="https://damp-lynna-wsep-1984852e.koyeb.app/";

    private final RestTemplate restTemplate;

    public TicketGateway(RestTemplate restTemplate)
    {
        this.restTemplate=restTemplate;
    }

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

    @Override
    public void revokeTicket(String externalTicketID)
    {
        if(externalTicketID==null || externalTicketID.isBlank())
            throw new IllegalArgumentException("Ticket cannot be blank.");
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("action_type","cancel_ticket");
        requestBody.add("ticket_id", externalTicketID);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        //send and get the response
        final ResponseEntity<String> response;

        try{
            response = restTemplate.postForEntity(BASE_URL, requestEntity, String.class);
        }catch (RestClientException  e){
            throw new TicketRevokeUnknownStatusException("Failed to contact ticket provider", e);
        }

        String responseBody = response.getBody();
        if(responseBody == null || responseBody.isBlank()) {
            throw new TicketRevokeUnknownStatusException("Payment provider returned empty response");
        }

        final int revokeResult;
        try
        {
            revokeResult = Integer.parseInt(responseBody.trim());
        }
        catch(NumberFormatException e)
        {
            throw new TicketRevokeUnknownStatusException("Invalid response from ticket provider: " + responseBody,e);
        }

        if(revokeResult == -1)
        {
            throw new RefundFailedException("revoke failed for ticket: " + externalTicketID);
        }

        if(revokeResult != 1)
        {
            throw new TicketRevokeUnknownStatusException("Provider returned invalid ticket revoke result: " + revokeResult + ", for ticket: " + externalTicketID);
        }

    }

}
