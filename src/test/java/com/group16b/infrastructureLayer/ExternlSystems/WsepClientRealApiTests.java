package com.group16b.infrastructureLayer.ExternlSystems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

@Tag("external")
public class WsepClientRealApiTests {
    private WsepClient wsepClient=new WsepClient(new RestTemplate());

    @Test
    void test_handshake()
    {
        assertDoesNotThrow(()->wsepClient.handshake());
    }
    
}
