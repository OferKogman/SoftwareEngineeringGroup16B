package com.group16b.ApiLayer;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;

import com.group16b.ApplicationLayer.EventService;
import com.group16b.ApplicationLayer.Objects.Result;

public class BaseController {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);
    private static final String SERVICE_DOWN ="Service temporarily unavailable. Please try again later";

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
            logger.error("Base controller: DataAccessException: ",e);
            return ResponseEntity.internalServerError().body(SERVICE_DOWN);

        } catch (Exception e) {
            logger.error("Base controller: Unexpected issue: ",e);
            return ResponseEntity.internalServerError().body(SERVICE_DOWN);
        }
    }

    
}
