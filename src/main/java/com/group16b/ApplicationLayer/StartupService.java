package com.group16b.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.group16b.ApplicationLayer.Exceptions.SystemStartupException;
import com.group16b.ApplicationLayer.Exceptions.WsepCommunicationException;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.SystemAdmin.SystemAdmin;
import com.group16b.InfrastructureLayer.ExternalSystems.WsepClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.group16b.DomainLayer.Interfaces.IRepository;
import com.group16b.DomainLayer.Venue.Venue;
import com.group16b.DomainLayer.Venue.Location;
import com.group16b.DomainLayer.Venue.FieldSeg;
import com.group16b.DomainLayer.Venue.ChosenSeatingSeg;
import com.group16b.DomainLayer.Venue.Segment;
import com.group16b.DomainLayer.Venue.Seat;
import com.group16b.DomainLayer.Venue.VenueGrid;
import com.group16b.DomainLayer.Venue.GridRectangle;
import com.group16b.DomainLayer.Venue.Stage;
import com.group16b.DomainLayer.Venue.Entrance;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;

@Service
public class StartupService {
    private final static Logger logger = LoggerFactory.getLogger(StartupService.class);
    private final IRepository<SystemAdmin> adminRepo;
    private final WsepClient wsepClient;
    private final InitialStateExecutor initialStateExecutor;
    private final IRepository<Venue> venueRepository;

    @Value("${config.path:config/system.properties}")
    private String configFilePath;

    private int passNum = 50;
    private String wsepBaseUrl = null;


    //will grow as more invariants would be needed to validate
    public StartupService(IRepository<SystemAdmin> adminRepo, WsepClient wsepClient,
                          InitialStateExecutor initialStateExecutor,
                          IRepository<Venue> venueRepository) {
        this.adminRepo = adminRepo;
        this.wsepClient = wsepClient;
        this.initialStateExecutor = initialStateExecutor;
        this.venueRepository = venueRepository;
    }

    //used for the test for now
    void setConfigFilePath(String path) { this.configFilePath = path; }

    //check and fix basic invariants of the system, such as existence of a default system admin, and more in the future
    @Transactional
    public void initializeSystem() {
        logger.info("StartupService.initializeSystem: Starting system initialization...");
        readAndValidateConfig();
        validateAdmins();
        validateExternalDependencies();
        seedDefaultVenue();
        executeInitialStateFile();
    }

    //-------------------- VALIDATORS --------------------//
    //add more validators here as needed, such as validating the existence of at least one admin, etc.
    //will probably grow significantly when we move to dbs, as we will need to validate the correctness of our data as well.
    private void validateAdmins()
    {
        try{
            if(adminRepo.getAll().isEmpty()) {
                logger.info("StartupService.validateAdmins: No system admins found. Creating default system admin...");
                SystemAdmin defaultAdmin = new SystemAdmin("admin123","password","mail@example.com");
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

    private void readAndValidateConfig() {
        logger.info("StartupService.readAndValidateConfig: Reading config file: {}", configFilePath);
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            props.load(fis);
        } catch (IOException e) {
            throw new SystemStartupException("Config file not found or unreadable: " + configFilePath, e);
        }

        String passNumStr = props.getProperty("queue.pass-num");
        if (passNumStr == null || passNumStr.isBlank())
            throw new SystemStartupException("Missing required config property: queue.pass-num", null);
        try {
            passNum = Integer.parseInt(passNumStr.trim());
            if (passNum <= 0)
                throw new SystemStartupException("queue.pass-num must be positive, got: " + passNumStr, null);
        } catch (NumberFormatException e) {
            throw new SystemStartupException("queue.pass-num must be an integer, got: " + passNumStr, e);
        }

        wsepBaseUrl = props.getProperty("wsep.base-url");
        if (wsepBaseUrl == null || wsepBaseUrl.isBlank())
            throw new SystemStartupException("Missing required config property: wsep.base-url", null);

        logger.info("StartupService.readAndValidateConfig: Config loaded — queue.pass-num={}, wsep.base-url={}",
                passNum, wsepBaseUrl);
    }

    private void executeInitialStateFile() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            props.load(fis);
        } catch (IOException e) {
            throw new SystemStartupException("Failed to re-read config file: " + configFilePath, e);
        }

        String initialStateFile = props.getProperty("initial-state.file");
        if (initialStateFile == null || initialStateFile.isBlank()) {
            logger.info("StartupService.executeInitialStateFile: No initial state file configured — skipping.");
            return;
        }

        logger.info("StartupService.executeInitialStateFile: Executing: {}", initialStateFile);
        initialStateExecutor.execute(initialStateFile.trim());
        logger.info("StartupService.executeInitialStateFile: Done.");
    }

    private void seedDefaultVenue() {
        try {
            if (!venueRepository.getAll().isEmpty()) {
                logger.info("StartupService.seedDefaultVenue: Venue already exists, skipping.");
                return;
            }
            logger.info("StartupService.seedDefaultVenue: Seeding default venue...");

            FieldSeg standingZone = new FieldSeg("standingZone", 30, new GridRectangle(0, 0, 2, 5));

            Map<String, Seat> seats = new HashMap<>();
            for (int row = 1; row <= 10; row++) {
                for (int col = 1; col <= 10; col++) {
                    String seatId = (char)('A' + row - 1) + "-" + col;
                    seats.put(seatId, new Seat((char)('A' + row - 1), col));
                }
            }
            ChosenSeatingSeg seatingZone = new ChosenSeatingSeg("seatingZone", seats, new GridRectangle(3, 0, 13, 10));

            Map<String, Segment> segments = new HashMap<>();
            segments.put(standingZone.getSegmentID(), standingZone);
            segments.put(seatingZone.getSegmentID(), seatingZone);

            Location location = new Location("Venue1", "1", "Main Street", "Tel Aviv",
                    "Tel Aviv", "Israel", 32.0853, 34.7818);

            Venue venue = new Venue("Venue1", location, segments, "venue1",
                    new VenueGrid(15, 10),
                    new ConcurrentHashMap<>(),
                    new ConcurrentHashMap<>(), 0);

            venueRepository.save(venue);
            logger.info("StartupService.seedDefaultVenue: Default venue seeded with ID: {}", venue.getID());

        } catch (Exception e) {
            logger.error("StartupService.seedDefaultVenue: Failed to seed default venue.", e);
            throw new SystemStartupException("Failed to seed default venue.", e);
        }
    }


}
