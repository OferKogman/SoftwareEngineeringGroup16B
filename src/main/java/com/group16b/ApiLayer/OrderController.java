package com.group16b.ApiLayer;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.DTOs.CompleteActiveOrderRequestDTO;
import com.group16b.ApplicationLayer.OrderService;
import com.group16b.ApplicationLayer.Records.PaymentInfo;

@RestController
@RequestMapping("/api/order")
public class OrderController extends BaseController{
    private final OrderService orderService;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PutMapping("/completeActiveOrder")
    public ResponseEntity<?> CompleteActiveOrder(@RequestHeader("Authorization") String sessionToken, @RequestBody CompleteActiveOrderRequestDTO requestDTO){
        PaymentInfo newPlymentInfo = new PaymentInfo("ILS", requestDTO.getPaymentInfo().cardNumber(), requestDTO.getPaymentInfo().month(), requestDTO.getPaymentInfo().year(), requestDTO.getPaymentInfo().holder(), requestDTO.getPaymentInfo().cvv(), requestDTO.getPaymentInfo().id());
        return executeWithReturnData(() -> orderService.CompleteActiveOrder(requestDTO.getOrderID(), sessionToken, newPlymentInfo));
    }
    
    @PutMapping("/changeSeatsToOrder/{orderId}")
    public ResponseEntity<?> changeSeatsToOrder(@RequestHeader("Authorization") String sessionToken, @PathVariable("orderId") String orderId, @RequestBody List<String> newSeatIds){
        return executeWithReturnData(() -> orderService.changeSeatsToOrder(orderId, sessionToken, newSeatIds));
    }

    @PutMapping("/changeNumOfSeatsInFieldOrder/{orderId}")
    public ResponseEntity<?> changeNumOfSeatsInFieldOrder(@RequestHeader("Authorization") String sessionToken, @PathVariable("orderId") String orderId, @RequestBody int newSeatsNum){
        return executeWithReturnData(() -> orderService.changeNumOfSeatsInFieldOrder(orderId, sessionToken, newSeatsNum));
    }

    @PutMapping("/cancelOrder/{orderId}")
    public ResponseEntity<?> cancelOrder(@RequestHeader("Authorization") String sessionToken, @PathVariable("orderId") String orderId){
        return executeWithReturnData(() -> orderService.cancelOrder(orderId, sessionToken));
    }
}
