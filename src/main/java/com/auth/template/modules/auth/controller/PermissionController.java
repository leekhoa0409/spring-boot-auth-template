package com.auth.template.modules.auth.controller;

import com.auth.template.common.dto.ApiResponse;
import com.auth.template.modules.auth.dto.request.PermissionRequest;
import com.auth.template.modules.auth.dto.response.PermissionResponse;
import com.auth.template.modules.auth.service.PermissionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("api/admin/permissions")
@AllArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final RestClient.Builder builder;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        ApiResponse<List<PermissionResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(permissions);
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> addPermission(@RequestBody PermissionRequest permissionRequest) {
        PermissionResponse savedPermissionResponse = permissionService.createPermission(permissionRequest);
        ApiResponse<PermissionResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(savedPermissionResponse);
        return ResponseEntity.ok().body(apiResponse);
    }

    @DeleteMapping("{permission}")
    public ResponseEntity<ApiResponse<Boolean>> deletePermission(@PathVariable String permission) {
        if (permissionService.deletePermission(permission)) {
            ApiResponse<Boolean> apiResponse = new ApiResponse<>();
            apiResponse.setResult(true);
            return ResponseEntity.ok().body(apiResponse);
        }
        return ResponseEntity.notFound().build();
    }
}
