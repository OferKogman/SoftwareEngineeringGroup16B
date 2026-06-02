package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.VenueEventConfigService;
import com.group16b.ApplicationLayer.DTOs.ConfigureLayoutAndInventoryDTO;
import com.group16b.ApplicationLayer.DTOs.ConfigureNewLayoutAndInventoryDTO;

@RestController
@RequestMapping("/venue/eventConfig")
public class VenueEventConfigController extends BaseController{
    private final VenueEventConfigService venueEventConfigService;
    
    public VenueEventConfigController(VenueEventConfigService venueEventConfigService){
        this.venueEventConfigService = venueEventConfigService;
    }

    @PostMapping("/configureNewLayoutAndInventory")
    public ResponseEntity<?> configureNewLayoutAndInventory(@RequestHeader("Authorization") String sessionToken, @RequestBody ConfigureNewLayoutAndInventoryDTO requestDTO){
        return executeWithReturnData(() -> venueEventConfigService.configureNewLayoutAndInventory(sessionToken, requestDTO.getCompanyID(), requestDTO.getEventID(), requestDTO.getNewVenueLayout()));
    }

    @PutMapping("/configureLayoutAndInventory")
    public ResponseEntity<?> configureLayoutAndInventory(@RequestHeader("Authorization") String sessionToken, @RequestBody ConfigureLayoutAndInventoryDTO requestDTO){
        return executeWithReturnData(() -> venueEventConfigService.configureLayoutAndInventory(sessionToken, requestDTO.getCompanyID(), requestDTO.getEventID(), requestDTO.getVenueID()));
    }
}
