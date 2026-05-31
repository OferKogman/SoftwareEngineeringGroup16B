package com.group16b.ApiLayer;

import java.util.function.Supplier;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.EventService;
import com.group16b.ApplicationLayer.Records.EventRecord;

@RestController
@RequestMapping("/events")
public class EventController extends BaseController {
    private final EventService eventService;
    public EventController(EventService eventService) {
        this.eventService = eventService;
     }

     @PostMapping
     public ResponseEntity<?> createEvent(
        @RequestHeader("Authorization") String authToken, 
        @RequestBody EventRecord request)
        {
            return executeWithReturnData(() -> eventService.createEvent(request, authToken));
    }

    @PostMapping("/{eventID}/activate")
    public ResponseEntity<?> activateEvent(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventID") int eventID
    ) {
        return executeWithNoReturnData(() -> eventService.activateEvent(eventID, authToken));
    }

    @PostMapping("/{eventID}/deactivate")
    public ResponseEntity<?> deactivateEvent(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventID") int eventID
    ) {
        return executeWithNoReturnData(() -> eventService.deactivateEvent(eventID, authToken));
    }
}
