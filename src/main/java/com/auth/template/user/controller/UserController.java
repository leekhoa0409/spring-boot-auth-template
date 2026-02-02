package com.auth.template.user.controller;

import com.auth.template.auth.dto.request.ChangePasswordRequest;
import com.auth.template.auth.dto.response.UserResponse;
import com.auth.template.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User profile management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = userService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserResponse updateRequest) {
        UserResponse response = userService.updateProfile(userDetails.getUsername(), updateRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    // @DeleteMapping("/me")
    // @Operation(summary = "Delete account")
    // public ResponseEntity<Map<String, String>>
    // deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
    // userService.deleteAccount(userDetails.getUsername());
    // return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    // }
}
