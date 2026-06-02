package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.DTOs.AdminLoginRequestDTO;
import com.group16b.ApplicationLayer.SystemAdminLoginService;

@RestController
@RequestMapping("/api/admin")
public class SystemAdminLoginController extends BaseController{
    private final SystemAdminLoginService systemAdminLoginService;

    public SystemAdminLoginController(SystemAdminLoginService systAdminLoginService){
        this.systemAdminLoginService = systAdminLoginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody AdminLoginRequestDTO requestDTO,
                                        @RequestHeader("Authorization") String guestSessionToken
    ) {


        return executeWithReturnData(() -> systemAdminLoginService.loginAdmin(requestDTO.getUsername(), requestDTO.getPassword(), requestDTO.getEmail(), guestSessionToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutAdmin(@RequestHeader("Authorization") String sessionToken) {


        return executeWithReturnData(() -> systemAdminLoginService.logOutAdmin(sessionToken));
    }
}