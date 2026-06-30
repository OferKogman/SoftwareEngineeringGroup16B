package com.group16b.ApiLayer;

import java.util.function.Supplier;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;

import com.group16b.ApplicationLayer.Objects.Result;

public class BaseController {
    protected <T> ResponseEntity<?> executeWithReturnData(Supplier<Result<T>> action) {
        try {
            System.out.println("base controller,right before get");
            Result<T> result = action.get();

            if (result.isSuccess()) {
                return ResponseEntity.ok(result.getValue());
            }
            else{
                return ResponseEntity.badRequest().body(result.getError());
            }

        } catch (DataAccessException e) {
            System.out.println("Base Controller: DataAccessException: "+e.getMessage()+", type: "+e.getClass());
            return ResponseEntity.internalServerError().body("Service temporarily unavailable. Please try again later");
        } catch (Exception e) {
            System.out.println("Base Controller: Unexpected exception: "+e.getMessage()+", type: "+e.getClass());
            return ResponseEntity.internalServerError().body("Service temporarily unavailable. Please try again later");
        }
    }

    protected <T> ResponseEntity<?> executeWithNoReturnData(Supplier<Result<T>> action) {
        try {
            Result<T> result = action.get();

            if (result.isSuccess()) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.badRequest().body(result.getError());
        } catch (DataAccessException e) {
            System.out.println("Base Controller: DataAccessException: "+e.getMessage()+", type: "+e.getClass());
            return ResponseEntity.internalServerError().body("Service temporarily unavailable. Please try again later");
        } catch (Exception e) {
            System.out.println("Base Controller: Unexpected exception: "+e.getMessage()+", type: "+e.getClass());
            return ResponseEntity.internalServerError().body("Service temporarily unavailable. Please try again later");
        }
    }

    
}
