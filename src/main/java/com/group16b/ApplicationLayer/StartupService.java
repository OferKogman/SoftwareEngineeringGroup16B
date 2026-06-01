package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Interfaces.IPaymentGateway;
import com.group16b.ApplicationLayer.Interfaces.ITicketGateway;
import com.group16b.DomainLayer.SystemAdmin.ISystemAdminRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StartupService {
    private final static Logger logger = LoggerFactory.getLogger(StartupService.class);
    private final ISystemAdminRepository adminRepo;
    private final IPaymentGateway paymentGateway;
    private final ITicketGateway ticketGateway;


    //will grow as more invariants would be needed to validate
    public StartupService(ISystemAdminRepository adminRepo,IPaymentGateway paymentGateway, ITicketGateway ticketGateway) {
        this.adminRepo = adminRepo;
        this.paymentGateway = paymentGateway;
        this.ticketGateway = ticketGateway;
    }

    //check and fix basic invariants of the system, such as existence of a default system admin, and more in the future
    @Transactional
    public void initializeSystem() {
        logger.info("StartupService.initializeSystem: Starting system initialization...");
        validateAdmins();
        validatePaymentGateway();
        validateTicketGateway();
    }

    //-------------------- VALIDATORS --------------------//
    //add more validators here as needed, such as validating the existence of at least one admin, etc.
    //will probably grow significantly when we move to dbs, as we will need to validate the correctness of our data as well.
    private void validateAdmins()
    {
        try{
            if(adminRepo.getAll().isEmpty()) {
                logger.info("StartupService.validateAdmins: No system admins found. Creating default system admin...");
                SystemAdmin defaultAdmin = new SystemAdmin("0", "admin123","password","mail@example.com");
                adminRepo.save(defaultAdmin);
            }
        } catch (Exception e) {
            logger.error("StartupService.validateAdmins: Error occurred while initializing system.", e);
        }
    }
    private void validatePaymentGateway() {
        // Implement payment gateway validation logic here
    }
    private void validateTicketGateway() {
        // Implement ticket gateway validation logic here
    }
}
