package com.group16b.infrastructureLayer.ExternlSystems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.group16b.ApplicationLayer.Exceptions.WsepCommunicationException;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

public class WsepClientTests {

    private RestTemplate restTemplate;
    private WsepClient wsepClient;

    private final String HANSHAKE_EXPECTED_RESPONSE="OK";
    private final String BAD_HANDSHAKE_RESPONSE=" Its a me, ROBERTO";

    @BeforeEach
    void startup()
    {
        restTemplate=mock(RestTemplate.class);
        wsepClient=new WsepClient(restTemplate, "http://localhost:9999/");
        ResponseEntity<String> response =new ResponseEntity<>(HANSHAKE_EXPECTED_RESPONSE, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(response);
    }

    @Test
    void GoodHanshake_success()
    {
        assertDoesNotThrow(() -> wsepClient.handshake());
        verify(restTemplate).postForEntity(anyString(), any(), any());
    }

    @Test
    void cantContact_error()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenThrow(new RestClientException("Connection failed"));
        WsepCommunicationException e=assertThrows(WsepCommunicationException.class, ()->wsepClient.handshake());
        assertEquals("Failed to contact WSEP server during handshake.", e.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ","   "})
    void blankRespone_error(String msg)
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(msg, HttpStatus.OK));
        WsepCommunicationException e=assertThrows(WsepCommunicationException.class, ()->wsepClient.handshake());
        assertEquals("Received empty response during WSEP handshake.", e.getMessage());
    }

    @Test
    void unknownRespose_error()
    {
        when(restTemplate.postForEntity(anyString(),any(HttpEntity.class),eq(String.class))).thenReturn(new ResponseEntity<>(BAD_HANDSHAKE_RESPONSE, HttpStatus.OK));
        WsepCommunicationException e=assertThrows(WsepCommunicationException.class, ()->wsepClient.handshake());
        assertEquals("Handshake response expected: OK, instead got: Its a me, ROBERTO", e.getMessage());
    }

    


    
}
