package com.group16b.InfrastructureLayer.ExternalSystems;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.group16b.ApplicationLayer.Exceptions.WsepCommunicationException;

@Component
public class WsepClient
{
    private final String baseUrl;
    private final String HANDSHAKE_OK_RESPONSE="OK";

    private final RestTemplate restTemplate;

    public WsepClient(RestTemplate restTemplate, @Value("${wsep.base-url}") String baseUrl)
    {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public String sendRequest(MultiValueMap<String, String> requestBody,Function<RestClientException, RuntimeException> communicationExceptionFactory,Supplier<RuntimeException> emptyResponseExceptionFactory)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =new HttpEntity<>(requestBody, headers);
        final ResponseEntity<String> response;

        try
        {
            response = restTemplate.postForEntity(baseUrl, request, String.class);
        }
        catch(RestClientException e)
        {
            throw communicationExceptionFactory.apply(e);
        }

        String body = response.getBody();

        if(body == null || body.isBlank())
        {
            throw emptyResponseExceptionFactory.get();
        }

        return body;
    }

    public int parseIntegerResponse(String responseBody,Function<String, RuntimeException> invalidResponseExceptionFactory)
    {
        try
        {
            return Integer.parseInt(responseBody.trim());
        }
        catch(NumberFormatException e)
        {
            throw invalidResponseExceptionFactory.apply(responseBody);
        }
    }

    public void validateSuccessFailureResult(int result,Supplier<RuntimeException> failedExceptionFactory,Supplier<RuntimeException> unknownStatusFactory)
    {
        if(result == -1)
            throw failedExceptionFactory.get();

        if(result != 1)
            throw unknownStatusFactory.get();
    }

    public void handshake()
    {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("action_type","handshake");
        String response=sendRequest(requestBody, e->new WsepCommunicationException("Failed to contact WSEP server during handshake.",e), ()->new WsepCommunicationException("Received empty response during WSEP handshake.")).trim();
        if(!HANDSHAKE_OK_RESPONSE.equals(response))
            throw new WsepCommunicationException("Handshake response expected: "+HANDSHAKE_OK_RESPONSE+", instead got: "+response);

    }
}