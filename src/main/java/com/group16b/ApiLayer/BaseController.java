package com.group16b.ApiLayer;

import java.util.function.Supplier;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;

import com.group16b.ApplicationLayer.Objects.Result;

public class BaseController {
    protected <T> ResponseEntity<?> executeWithReturnData(Supplier<Result<T>> action) {
        return execute(action,result -> ResponseEntity.ok(result.getValue()));
    }

    protected <T> ResponseEntity<?> executeWithNoReturnData(Supplier<Result<T>> action) {
        return execute(action, result -> ResponseEntity.ok().build());
    }

    protected <T> ResponseEntity<?> execute(Supplier<Result<T>> action,
            java.util.function.Function<Result<T>, ResponseEntity<?>> onSuccess) {
        try {
            Result<T> result = action.get();

            if (result.isSuccess()) {
                return onSuccess.apply(result);
            }

            return ResponseEntity.badRequest().body(result.getError());

        } catch (DataAccessException e) {
            System.out.println("Base Controller: DataAccessException: " + e.getMessage()+", exception type: "+e.getClass());
            System.out.println(e);
            return ResponseEntity.internalServerError().body("Service temporarily unavailable. Please try again later");

        } catch (Exception e) {
            System.out.println("Base Controller: Unexpected exception: " + e.getMessage()+", exception type: "+e.getClass());
            System.out.println(e);
            return ResponseEntity.internalServerError().body("Service temporarily unavailable. Please try again later");
        }
    }

    
}
