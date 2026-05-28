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
            return ResponseEntity.ok(salesHistory);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    
}
