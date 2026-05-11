package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.InfrastructureLayer.AuthenticationServiceJWTImpl;
import com.group16b.InfrastructureLayer.LocationServicePhotonImpl;
import com.group16b.InfrastructureLayer.PaymentService;
import com.group16b.InfrastructureLayer.TicketGateway;

public class StartupService {
    private static Logger logger = LoggerFactory.getLogger(StartupService.class);


    public void initialize() {
        AuthenticationServiceJWTImpl authService = new AuthenticationServiceJWTImpl("mySuperSecretKeyForUsers123456789", "mySuperSecretKeyForAdmins123456789");
        LocationServicePhotonImpl locationService = new LocationServicePhotonImpl();
        PaymentService paymentService = new PaymentService();
        TicketGateway ticketGateway = new TicketGateway();
        
        // Initialize necessary components, load data, etc.
        // For example, you might want to load events, users, etc. from a database or file here.
    }
}
