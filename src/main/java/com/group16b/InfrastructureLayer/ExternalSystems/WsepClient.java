package com.group16b.InfrastructureLayer.ExternalSystems;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class WsepClient
{
    private static final String BASE_URL ="https://damp-lynna-wsep-1984852e.koyeb.app/";

    private final RestTemplate restTemplate;

    public WsepClient(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }

    public String sendRequest(MultiValueMap<String, String> requestBody,Function<RestClientException, RuntimeException> communicationExceptionFactory,Supplier<RuntimeException> emptyResponseExceptionFactory)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request =new HttpEntity<>(requestBody, headers);
        final ResponseEntity<String> response;

        try
        {
            response = restTemplate.postForEntity(BASE_URL,request,String.class);
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

        return body.trim();
    }

    public int parseIntegerResponse(String responseBody,Function<String, RuntimeException> invalidResponseExceptionFactory)
    {
        try
        {
            return Integer.parseInt(responseBody);
        }
        catch(NumberFormatException e)
        {
            throw invalidResponseExceptionFactory.apply(responseBody);
        }
    }

    public void validateBinaryResult(int result,Supplier<RuntimeException> failedExceptionFactory,Supplier<RuntimeException> unknownStatusFactory)
    {
        if(result == -1)
            throw failedExceptionFactory.get();

        if(result != 1)
            throw unknownStatusFactory.get();
    }
}