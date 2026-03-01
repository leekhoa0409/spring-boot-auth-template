package com.auth.template.modules.user.service;

import com.auth.template.mapper.RoleMapper;
import com.auth.template.modules.user.dto.request.RoleRequest;
import com.auth.template.modules.user.dto.response.RoleResponse;
import com.auth.template.modules.user.entity.Permission;
import com.auth.template.modules.user.entity.Role;
import com.auth.template.modules.user.repository.PermissionRepository;
import com.auth.template.modules.user.repository.RoleRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;

    @Transactional
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(roleMapper::toRoleResponse).toList();
    }

    public RoleResponse createRole(RoleRequest roleRequest) {
        Role role = roleMapper.toRole(roleRequest);
        List<Permission> permissions = permissionRepository.findAllById(roleRequest.getPermissions());
        log.info("Permissions found: {}", permissions);
        role.setPermissions(new HashSet<>(permissions));
        Role savedRole = roleRepository.save(role);
        return roleMapper.toRoleResponse(savedRole);
    }

    public RoleResponse updateRole(RoleRequest roleRequest) {
        Role newRole = roleMapper.toRole(roleRequest);
        List<Permission> permissions = permissionRepository.findAllById(roleRequest.getPermissions());
        newRole.setPermissions(new HashSet<>(permissions));
        Role savedRole = roleRepository.save(newRole);
        return roleMapper.toRoleResponse(savedRole);
    }

    public boolean deleteRole(String roleName) {
        if (roleRepository.existsById(roleName)) {
            roleRepository.deleteById(roleName);
            return true;
        }
        return false;
    }
}
