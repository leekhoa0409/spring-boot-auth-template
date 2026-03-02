package com.auth.template.modules.auth.service;

import com.auth.template.mapper.PermissionMapper;
import com.auth.template.modules.auth.dto.request.PermissionRequest;
import com.auth.template.modules.auth.dto.response.PermissionResponse;
import com.auth.template.modules.auth.entity.Permission;
import com.auth.template.modules.auth.repository.PermissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public PermissionResponse createPermission(PermissionRequest permissionRequest) {
        Permission permission = permissionMapper.toPermission(permissionRequest);
        permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public PermissionResponse updatePermission(PermissionRequest permissionRequest) {
        Permission newPermission = permissionMapper.toPermission(permissionRequest);
        permissionRepository.save(newPermission);
        return permissionMapper.toPermissionResponse(newPermission);
    }

    public boolean deletePermission(String permission) {
        if (permissionRepository.existsById(permission)) {
            permissionRepository.deleteById(permission);
            return true;
        }
        return false;
    }
}
