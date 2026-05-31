package com.group16b.PresentationLayer.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.DTOs.LoginRequestDTO;
import com.group16b.ApplicationLayer.Objects.Result;
import com.group16b.ApplicationLayer.UserLoginService;

@RestController
@RequestMapping("/api/user/login")
public class UserLoginController {

    private final UserLoginService userLoginService;

    public UserLoginController(UserLoginService userLoginService) {
        this.userLoginService = userLoginService;
    }

    @PostMapping("/member")
    public ResponseEntity<Result<String>> loginMember(@RequestBody LoginRequestDTO requestDTO) {
        try {
            Result<String> loginResult = userLoginService.loginMember(
                requestDTO.getEmail(), 
                requestDTO.getPassword()
            );

            if (loginResult.isSuccess()) {
                return ResponseEntity.ok(loginResult);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(loginResult);
            }

        } catch (Exception ex) {
            Result<String> errorResult = Result.makeFail("System error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<Result<String>> loginGuest() {
        try {
            Result<String> loginResult = userLoginService.createGuestSession();

            if (loginResult.isSuccess()) {
                return ResponseEntity.ok(loginResult);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(loginResult);
            }

        } catch (Exception ex) {
            Result<String> errorResult = Result.makeFail("System error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }
}