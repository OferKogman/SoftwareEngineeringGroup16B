package com.group16b.ApiLayer;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.DTOs.ConfigureLayoutAndInventoryDTO;
import com.group16b.ApplicationLayer.DTOs.ConfigureNewLayoutAndInventoryDTO;
import com.group16b.ApplicationLayer.Records.ChosenSeatingSegRecord;
import com.group16b.ApplicationLayer.Records.FieldSegRecord;
import com.group16b.ApplicationLayer.VenueEventConfigService;

@RestController
@RequestMapping("/venues")
public class VenueEventConfigController extends BaseController {
    private final VenueEventConfigService venueEventConfigService;

    public VenueEventConfigController(VenueEventConfigService venueEventConfigService) {
        this.venueEventConfigService = venueEventConfigService;
    }

    @PostMapping("/configureNewLayoutAndInventory")
    public ResponseEntity<?> configureNewLayoutAndInventory(@RequestHeader("Authorization") String sessionToken,
            @RequestBody ConfigureNewLayoutAndInventoryDTO requestDTO) {
        return executeWithReturnData(() -> venueEventConfigService.configureNewLayoutAndInventory(sessionToken,
                requestDTO.getCompanyID(), requestDTO.getNewVenueLayout()));
    }

    @PutMapping("/configureLayoutAndInventory")
    public ResponseEntity<?> configureLayoutAndInventory(@RequestHeader("Authorization") String sessionToken,
            @RequestBody ConfigureLayoutAndInventoryDTO requestDTO) {
        return executeWithReturnData(() -> venueEventConfigService.configureLayoutAndInventory(sessionToken,
                requestDTO.getCompanyID(), requestDTO.getEventID(), requestDTO.getVenueID()));
    }

    @GetMapping("/{venueID}/location")
    public ResponseEntity<?> getVenue(@RequestHeader("Authorization") String sessionToken,
            @PathVariable("venueID") String venueID) {
        return executeWithReturnData(() -> venueEventConfigService.getVenue(sessionToken, venueID));
    }

    @GetMapping("/{venueID}")
    public ResponseEntity<?> getVenue_(@RequestHeader("Authorization") String sessionToken,
            @PathVariable("venueID") String venueID) {
        System.out.println("Received request to get venue with ID: " + venueID);
        return executeWithReturnData(() -> venueEventConfigService.getVenue(sessionToken, venueID));
    }

    
    @GetMapping("/{venueID}/segments")
    public ResponseEntity<?> editVenueSegments(@RequestHeader("Authorization") String sessionToken,
                @PathVariable("venueID") String venueID,
                @RequestBody Map<String, Integer> fieldSegmentsToEdit,
                @RequestBody Map<String, List<String>> seatsSegmentsToEdit,
                @RequestBody List<FieldSegRecord> newFieldSegments,
                @RequestBody List<ChosenSeatingSegRecord> newSeatSegments,
                @PathVariable("companyID") int companyID
            ) {
        System.out.println("Received request to edit venue segments for venue ID: " + venueID);
        return executeWithReturnData(() -> venueEventConfigService.editVenueSegments(companyID, venueID,fieldSegmentsToEdit, seatsSegmentsToEdit, newFieldSegments, newSeatSegments, sessionToken));
    }
}
