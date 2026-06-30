package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.DTOs.LoginRequestDTO;
import com.group16b.InfrastructureLayer.Security.PublicEndpoint;
import com.group16b.ApplicationLayer.UserLoginService;

@RestController
@RequestMapping("/api/user/login")
public class UserLoginController extends BaseController{

    private final UserLoginService userLoginService;

    public UserLoginController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @PostMapping("/member")
    public ResponseEntity<?> loginMember(@RequestBody LoginRequestDTO requestDTO,
                                         @RequestHeader("Authorization") String guestSessionToken
    ) {
        return executeWithReturnData(() -> userLoginService.loginMember(requestDTO.getEmail(), requestDTO.getPassword(), guestSessionToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logOut(@RequestHeader("Authorization") String sessionToken) {
        
        return executeWithReturnData(() -> userLoginService.logOutMember(sessionToken));
    }

    @PublicEndpoint
    @PostMapping("/guest")
    public ResponseEntity<?> loginGuest() {
        return executeWithReturnData(() -> userLoginService.ensureGuestSession(null));
    }

    @PublicEndpoint
    @PostMapping("/guest/validate")
    public ResponseEntity<?> validateGuest(@RequestHeader(value = "Authorization", required = false) String sessionToken) {
        return executeWithReturnData(() -> userLoginService.ensureGuestSession(sessionToken));
    }
}