package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.ProductionCompanyService;
import com.group16b.ApplicationLayer.DTOs.CreateProductionCompanyRequestDTO;

@RestController
@RequestMapping("/production-companies")
public class ProductionCompanyController extends BaseController {
    private final ProductionCompanyService productionCompanyService;

    public ProductionCompanyController(ProductionCompanyService productionCompanyService) {
        this.productionCompanyService = productionCompanyService;
    }

    @GetMapping("/{companyId}/sales-history")
    public ResponseEntity<?> getSalesHistory(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId) 
    {
        return executeWithReturnData(() -> productionCompanyService.viewSalesHistory(authToken, companyId));
    }

    @GetMapping("/{companyId}/total-revenue")
    public ResponseEntity<?> getTotalRevenue(
                    @RequestHeader("Authorization") String authToken, 
                    @PathVariable("companyId") int companyId) 
    {
        return executeWithReturnData(() -> productionCompanyService.displayTotalRevenue(authToken, companyId));
    }

    @PostMapping
    public ResponseEntity<?> createProductionCompany(
        @RequestHeader("Authorization") String authToken,
        @RequestBody CreateProductionCompanyRequestDTO requestDTO)
    {
        return executeWithReturnData(() -> productionCompanyService.createProductionCompany(authToken,requestDTO.getCompanyName()));
    }
    
}
