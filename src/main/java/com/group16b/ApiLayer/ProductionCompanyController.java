package com.group16b.ApiLayer;
import java.util.List;

import javax.annotation.processing.Generated;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.ProductionCompanyService;
import com.group16b.ApplicationLayer.DTOs.OrderDTO;
import com.group16b.ApplicationLayer.DTOs.ProductionCompanyDTO;
import com.group16b.ApplicationLayer.Objects.Result;

@RestController
@RequestMapping("/production-companies")
public class ProductionCompanyController {
    private final ProductionCompanyService productionCompanyService;

    public ProductionCompanyController(ProductionCompanyService productionCompanyService) {
        this.productionCompanyService = productionCompanyService;
    }

    @GetMapping("/{companyId}/sales-history")
    public ResponseEntity<?> getSalesHistory(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable int companyId) 
    {
        try {
            Result<List<OrderDTO>> salesHistory = productionCompanyService.viewSalesHistory(authToken, companyId);
            if(salesHistory.isSuccess()) {
                return ResponseEntity.ok(salesHistory.getValue());
            } else {
                return ResponseEntity.badRequest().body(salesHistory.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{companyId}/total-revenue")
    public ResponseEntity<?> getTotalRevenue(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable int companyId) 
    {
        try {
            Result<Double> totalRevenue = productionCompanyService.displayTotalRevenue(authToken, companyId);
            if(totalRevenue.isSuccess()) {
                return ResponseEntity.ok(totalRevenue.getValue());
            } else {
                return ResponseEntity.badRequest().body(totalRevenue.getError());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{companyId}/create-company")
    public ResponseEntity<?> createProductionCompany(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable int companyId) 
    {
        try {
            Result<ProductionCompanyDTO> result = productionCompanyService.createProductionCompany(authToken, "New Company Name");
            if(result.isSuccess()) {
                return ResponseEntity.ok(result.getValue());
            } else {
                return ResponseEntity.badRequest().body(result.getError());
            }
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
    
}
