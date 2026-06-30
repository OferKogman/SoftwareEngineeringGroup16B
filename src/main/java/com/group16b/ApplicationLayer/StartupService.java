package com.group16b.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group16b.ApplicationLayer.Exceptions.SystemStartupException;
import com.group16b.ApplicationLayer.Exceptions.WsepCommunicationException;
import com.group16b.DomainLayer.Event.Event;
import com.group16b.DomainLayer.Event.IEventRepository;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.DomainLayer.VirtualQueue.VirtualQueue;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;
import org.springframework.beans.factory.annotation.Value;

@Service
@Transactional
public class StartupService {
    private final static Logger logger = LoggerFactory.getLogger(StartupService.class);
    private final IRepository<SystemAdmin> adminRepo;
    private final WsepClient wsepClient;
    private final IEventRepository eventRepo;
    private final IRepository<VirtualQueue> virtualQueueRepo;

    private final String defaultAdminUsername;
    private final String defaultAdminPassword;
    private final String defaultAdminEmail;
    private final boolean validateExternalSystems;


    //will grow as more invariants would be needed to validate
    public StartupService(
          IRepository<SystemAdmin> adminRepo,
          WsepClient wsepClient,
          IEventRepository eventRepo,
          IRepository<VirtualQueue> virtualQueueRepo,
          @Value("${system.default-admin.username}") String defaultAdminUsername,
          @Value("${system.default-admin.password}") String defaultAdminPassword,
          @Value("${system.default-admin.email}") String defaultAdminEmail,
          @Value("${startup.validate-external-systems}") boolean validateExternalSystems) {
            this.adminRepo = adminRepo;
            this.wsepClient = wsepClient;
            this.eventRepo = eventRepo;
            this.virtualQueueRepo = virtualQueueRepo;
            this.defaultAdminUsername = defaultAdminUsername;
            this.defaultAdminPassword = defaultAdminPassword;
            this.defaultAdminEmail = defaultAdminEmail;
            this.validateExternalSystems = validateExternalSystems;
    }

    //check and fix basic invariants of the system, such as existence of a default system admin, and more in the future
    public void initializeSystem() {
        logger.info("StartupService.initializeSystem: Starting system initialization...");
        validateAdmins();
        validateVirtualQueues();

        if (validateExternalSystems) {
            validateExternalDependencies();
        } else {
            logger.info("StartupService.initializeSystem: Skipping external dependency validation by configuration.");
        }
    }
    //--------------------SETUPERS------------------------//
    //should get he latest id from the db and set the gen to start from it +1
    private void initProductionCompanyIdGenerator()
    {

    }

    //-------------------- VALIDATORS --------------------//
    //add more validators here as needed, such as validating the existence of at least one admin, etc.
    //will probably grow significantly when we move to dbs, as we will need to validate the correctness of our data as well.
    private void validateAdmins()
    {
        try{
            if(adminRepo.getAll().isEmpty()) {
                logger.info("StartupService.validateAdmins: No system admins found. Creating default system admin...");
                SystemAdmin defaultAdmin = new SystemAdmin(defaultAdminUsername, defaultAdminPassword, defaultAdminEmail);
                adminRepo.save(defaultAdmin);
            }
        } catch (Exception e) {
            logger.error("StartupService.validateAdmins: Error occurred while initializing system.", e);
            throw new SystemStartupException("Failed to initialize system admins.", e); // Rethrow the exception to ensure the application fails to start if initialization fails
        }
    }
    private void validateExternalDependencies()
    {
        try{
            wsepClient.handshake();
        } catch(WsepCommunicationException e){
            logger.error("StartupService.validateExternalDependencies: WsepCommunicationException: WSEP handshake failed during startup validation. ",e);
            throw new SystemStartupException("Failed to validate external dependencies.",e);
        } catch(Exception e)
        {
            logger.error("StartupService.validateExternalDependencies: unexpected Error.",e);
            throw new SystemStartupException("An unexpected error occured during validating external dependencies avilability.",e);
        }
    }

    private void validateVirtualQueues() {
        logger.info("StartupService: Syncing in-memory Virtual Queues with database Events...");
        
        List<Event> allEvents = eventRepo.getAll();
        
        for (Event event : allEvents) {
            try {
                virtualQueueRepo.findByID(String.valueOf(event.getEventID())); 
            } catch (Exception e) {
                VirtualQueue rebuiltQueue = new VirtualQueue(event.getEventID());
                virtualQueueRepo.save(rebuiltQueue);
            }
        }
        logger.info("StartupService: Virtual Queues synced successfully.");
    }
}
