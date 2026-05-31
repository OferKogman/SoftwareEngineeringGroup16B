package com.group16b.ApiLayer;

import java.util.function.Supplier;

import org.springframework.http.ResponseEntity;

import com.group16b.ApplicationLayer.Objects.Result;

public class BaseController {
    protected <T> ResponseEntity<?> executeWithReturnData(Supplier<Result<T>> action) {
        try {
            Result<T> result = action.get();

            if (result.isSuccess()) {
                return ResponseEntity.ok(result.getValue());
            }
            else{
                return ResponseEntity.badRequest().body(result.getError());
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("System error: " +e.getMessage());
        }
    }

    protected <T> ResponseEntity<?> executeWithNoReturnData(Supplier<Result<T>> action) {
        try {
            Result<T> result = action.get();

            if (result.isSuccess()) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().body(result.getError());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("System error: " + e.getMessage());
        }
    }
}
