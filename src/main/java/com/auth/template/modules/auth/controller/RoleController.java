package com.auth.template.modules.auth.controller;

import com.auth.template.common.dto.ApiResponse;
import com.auth.template.modules.auth.dto.request.RoleRequest;
import com.auth.template.modules.auth.dto.response.RoleResponse;
import com.auth.template.modules.auth.service.RoleService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/admin/roles")
@Slf4j
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        ApiResponse<List<RoleResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(roles);
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> addRole(@RequestBody RoleRequest roleRequest) {
        RoleResponse savedRoleResponse = roleService.createRole(roleRequest);
        ApiResponse<RoleResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(savedRoleResponse);
        return ResponseEntity.ok().body(apiResponse);
    }

    @DeleteMapping("{role}")
    public ResponseEntity<ApiResponse<Boolean>> deleteRole(@PathVariable String role) {
        if (roleService.deleteRole(role)) {
            ApiResponse<Boolean> apiResponse = new ApiResponse<>();
            apiResponse.setResult(true);
            return ResponseEntity.ok().body(apiResponse);
        }
        return ResponseEntity.notFound().build();
    }
}
