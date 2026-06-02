package com.group16b.ApiLayer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.group16b.ApplicationLayer.UserService;
import com.group16b.ApplicationLayer.DTOs.LoginRequestDTO;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/registerUser") // loginRequestDTO has the same fields so I didnt add a seperate dto for
                                  // register
    public ResponseEntity<?> registerUser(@RequestBody LoginRequestDTO requestDTO) {
        return executeWithReturnData(() -> userService.registerUser(requestDTO.getEmail(), requestDTO.getPassword()));
    }

    @PutMapping("/updateUserPassword")
    public ResponseEntity<?> updateUserPassword(@RequestHeader("Authorization") String sessionToken,
            @RequestBody PasswordChangeDTO requestDTO) {
        return executeWithReturnData(() -> userService.updateUserPassword(sessionToken, requestDTO.getOldPassword(),
                requestDTO.getNewPassword()));
    }

    @GetMapping("/me/order-history")
    public ResponseEntity<?> getOrderShistory(@RequestHeader("Authorization") String sessionToken){
        return executeWithReturnData(() -> userService.getUserOrderHistory(sessionToken));
    }
}
