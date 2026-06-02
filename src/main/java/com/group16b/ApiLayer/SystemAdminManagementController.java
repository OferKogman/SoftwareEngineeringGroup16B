package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.AdminManagementService;
import com.group16b.ApplicationLayer.DTOs.AdminRegisterRequestDTO;

@RestController
@RequestMapping("/api/admin-management")
public class SystemAdminManagementController extends BaseController{
    private final AdminManagementService adminManagementService;
    
    public SystemAdminManagementController(AdminManagementService adminManagementService1){
        this.adminManagementService = adminManagementService1;
    }

    @GetMapping("/viewAllPurchasesHistory")
    public ResponseEntity<?> viewAllPurchesHistory(@RequestHeader("Authorization") String sessionToken) {
        return executeWithReturnData(() -> adminManagementService.viewAllPurchesHistory(sessionToken));
    }

    @GetMapping("/viewPurchesHistoryByCompany/{companyID}")
    public ResponseEntity<?> viewPurchesHistoryByCompany(@RequestHeader("Authorization") String sessionToken, @PathVariable("companyID") int  companyID){
        return executeWithReturnData(() -> adminManagementService.viewPurchesHistoryByCompany(sessionToken, companyID));
    }

    @GetMapping("/viewPurchesHistoryByUser/{userID}")
    public ResponseEntity<?> viewPurchesHistoryByUser(@RequestHeader("Authorization") String sessionToken, @PathVariable("userID") String  userID){
        return executeWithReturnData(() -> adminManagementService.viewPurchesHistoryByUser(sessionToken, userID));
    }

    @PutMapping("/closeProductionCompany/{companyID}")
    public ResponseEntity<?> closeProductionCompany(@RequestHeader("Authorization") String sessionToken, @PathVariable("companyID") int  companyID){
        return executeWithReturnData(() -> adminManagementService.closeProductionCompany(companyID, sessionToken));
    }

    @DeleteMapping("/removeUser/{userID}")
    public ResponseEntity<?> removeUser(@RequestHeader("Authorization") String sessionToken, @PathVariable("userID") String  userID){
        return executeWithReturnData(() -> adminManagementService.removeUser(userID, sessionToken));
    }

    @PostMapping("/registerNewAdmin")
    public ResponseEntity<?> registerNewAdmin(@RequestHeader("Authorization") String sessionToken, @RequestBody AdminRegisterRequestDTO requestDTO){
        return executeWithReturnData(() -> adminManagementService.registerNewAdmin(sessionToken, requestDTO.getNewAdminUsername(), requestDTO.getNewAdminPassword(), requestDTO.getNewAdminEmail()));
    }
}
