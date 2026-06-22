package com.group16b.ApplicationLayer.Records;

import com.group16b.ApplicationLayer.Enums.RefundStatus;

public record RefundResult(String orderId, RefundStatus status, String errorMessage ) {
    
}
