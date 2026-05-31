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

     private ResponseEntity<?> executeWithReturnData(Supplier<Result<?>> action) {
        try {
            Result<?> result = action.get();

            if (result.isSuccess()) {
                return ResponseEntity.ok(result.getValue());
            }

            return ResponseEntity.badRequest().body(result.getError());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    private ResponseEntity<?> executeWithNoReturnData(Supplier<Result<?>> action) {
        try {
            Result<?> result = action.get();

            if (result.isSuccess()) {
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.badRequest().body(result.getError());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
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
