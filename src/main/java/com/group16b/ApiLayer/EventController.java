package com.group16b.ApiLayer;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.EventService;
import com.group16b.ApplicationLayer.Records.EventRecord;
import com.group16b.ApplicationLayer.Records.EventSegmentConfigUpdateRecord;
import com.group16b.InfrastructureLayer.Security.PublicEndpoint;


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

    @PublicEndpoint
    @GetMapping("/{eventID}")
    public ResponseEntity<?> getEventDetails(
        @PathVariable("eventID") int eventID
    ) {
        return executeWithReturnData(() -> eventService.viewEvent(eventID));
    }

    @PatchMapping("/{eventID}")
    public ResponseEntity<?> editEvent(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventID") int eventID,
        @RequestBody Map<String, Object> request
    ) {
        return executeWithNoReturnData(() -> eventService.editEvent(request, eventID, authToken));
    }

    @PatchMapping("/{eventID}/segments/stock")
    public ResponseEntity<?> editEventStock(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventID") int eventID,
        @RequestBody Map<String, EventSegmentConfigUpdateRecord> request
    ) {
        return executeWithNoReturnData(() -> eventService.editStockInSegmentsForEvent(request, eventID, authToken));
    }

    @PublicEndpoint
    @PostMapping("/search")
    public ResponseEntity<?> searchEvents(
        @RequestBody Map<String, List<Object>> request
    ) {
        return executeWithReturnData(() -> eventService.searchEvents(request));
    }
}
