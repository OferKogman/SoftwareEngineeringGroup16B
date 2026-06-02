package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.DTOs.LoginRequestDTO;
import com.group16b.ApplicationLayer.UserLoginService;

@RestController
@RequestMapping("/api/user/login")
public class UserLoginController extends BaseController{

    private final UserLoginService userLoginService;

    public UserLoginController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @PostMapping("/member")
    public ResponseEntity<?> loginMember(@RequestBody LoginRequestDTO requestDTO) {
        
        return executeWithReturnData(() -> userLoginService.loginMember(requestDTO.getEmail(), requestDTO.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logOut(@RequestHeader("Authorization") String sessionToken) {
        
        return executeWithReturnData(() -> userLoginService.logOutMember(sessionToken));
    }

    @PostMapping("/guest")
    public ResponseEntity<?> loginGuest() {
        return executeWithReturnData(() -> userLoginService.createGuestSession());
    }
}