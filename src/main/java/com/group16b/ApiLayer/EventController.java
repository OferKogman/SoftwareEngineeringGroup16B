package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.EventService;
import com.group16b.ApplicationLayer.DTOs.EventDTO;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.Records.EventRecord;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;
    public EventController(EventService eventService) {
        this.eventService = eventService;
     }

     @PostMapping
     public ResponseEntity<?> createEvent(
        @RequestHeader("Authorization") String authToken, 
        @RequestBody EventRecord request)
        {
            try{
            Result<EventDTO> result = eventService.createEvent(request, authToken);
            if(result.isSuccess()) {
                return ResponseEntity.ok(result.getValue());
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    
}
