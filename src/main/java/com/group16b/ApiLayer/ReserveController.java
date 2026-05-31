package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.ReserveService;
import com.group16b.ApplicationLayer.DTOs.ReserveFieldSeatsRequestDTO;
import com.group16b.ApplicationLayer.DTOs.ReserveFieldSeatsWithLotteryRequestDTO;
import com.group16b.ApplicationLayer.DTOs.ReserveSeatsRequestDTO;
import com.group16b.ApplicationLayer.DTOs.ReserveSeatsWithLotteryRequestDTO;


@RestController
@RequestMapping("/events/{eventId}/reservations")
public class ReserveController extends BaseController {
    private final ReserveService reserveService;
    
    public ReserveController(ReserveService reserveService) {
        this.reserveService = reserveService;
     }

    @PostMapping("/seats")
    public ResponseEntity<?> reserveSeats(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventId") int eventId,
        @RequestBody ReserveSeatsRequestDTO request
    ) {
        return executeWithReturnData(() -> reserveService.reserveSeats(request.getSegmentId(), request.getSeatIds(), eventId,request.getVenueId(), authToken));
    }

    @PostMapping("/field")
    public ResponseEntity<?> reserveFieldSeats(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventId") int eventId,
        @RequestBody ReserveFieldSeatsRequestDTO request
    ) {
        return executeWithReturnData(() -> reserveService.reserveFieldSeats(request.getSegmentId(), request.getAmount(), eventId, request.getVenueId(), authToken));
    }

    @PostMapping("/seats/lottery")
    public ResponseEntity<?> reserveSeatsWithLottery(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventId") int eventId,
        @RequestBody ReserveSeatsWithLotteryRequestDTO request
    ) {
        return executeWithReturnData(() -> reserveService.reserveSeatsWithLottery(request.getSegmentId(), request.getSeatIds(), eventId, request.getVenueId(), request.getLotteryCode(), authToken));
    }

    @PostMapping("/field/lottery")
    public ResponseEntity<?> reserveFieldSeatsWithLottery(
        @RequestHeader("Authorization") String authToken,
        @PathVariable("eventId") int eventId,
        @RequestBody ReserveFieldSeatsWithLotteryRequestDTO request
    ) {
        return executeWithReturnData(() -> reserveService.reserveFieldSeatsWithLottery(request.getSegmentId(), request.getAmount(), eventId, request.getVenueId(), request.getLotteryCode(), authToken));
    }  


    
}
