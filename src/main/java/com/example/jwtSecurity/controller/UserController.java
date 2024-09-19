package com.example.jwtSecurity.controller;

import com.example.jwtSecurity.dto.UserDTO;
import com.example.jwtSecurity.model.AuthenticationResponse;
import com.example.jwtSecurity.model.User;
import com.example.jwtSecurity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody UserDTO request
    ) {
        return ResponseEntity.ok(userService.register(request));
    }
}
