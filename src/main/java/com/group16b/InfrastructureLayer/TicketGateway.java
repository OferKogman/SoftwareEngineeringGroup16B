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
import com.group16b.ApplicationLayer.Exceptions.RevokeTicketFailureException;
import com.group16b.ApplicationLayer.Exceptions.TicketGenerationException;
import com.group16b.ApplicationLayer.Exceptions.TicketRevokeUnknownStatusException;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

@Service
public class TicketGateway implements ITicketGateway{

    private static final String BASE_URL="https://damp-lynna-wsep-1984852e.koyeb.app/";

    private final WsepClient wsepClient;

    public TicketGateway(WsepClient wsepClient)
    {
        this.wsepClient=wsepClient;
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

        String responseBody=wsepClient.sendRequest(requestBody, 
                e -> new TicketRevokeUnknownStatusException("Failed to contact ticket provider when revoking ticket: "+externalTicketID, e), 
                () -> new TicketRevokeUnknownStatusException("ticket provider returned empty response when revoking ticket: "+externalTicketID));

        final int revokeResult=wsepClient.parseIntegerResponse(responseBody, body -> new TicketRevokeUnknownStatusException("Invalid response from ticket provider: " + body));
        
        wsepClient.validateSuccessFailureResult(revokeResult, 
            ()->new RevokeTicketFailureException("revoke failed for ticket: " + externalTicketID),
            ()-> new TicketRevokeUnknownStatusException("Provider returned invalid ticket revoke result: " + revokeResult + ", for ticket: " + externalTicketID));

    }

}
