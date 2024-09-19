package com.example.jwtSecurity.controller;

import com.example.jwtSecurity.dto.UserDTO;
import com.example.jwtSecurity.model.AuthenticationResponse;
import com.example.jwtSecurity.model.User;
import com.example.jwtSecurity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody UserDTO request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @GetMapping("/profile")
    public UserDTO getProfile() {
        return userService.getProfile();
    }

    @GetMapping("/admin/all")
    // Only allow admins to access this endpoint
    public List<User> getAllNonAdminUsers() {
        return userService.getAllNonAdminUsers();
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateUserProfile(@RequestBody UserDTO updatedUserDTO) {
        UserDTO updatedUser = userService.updateUser(updatedUserDTO);
        return ResponseEntity.ok(updatedUser);
    }
}
