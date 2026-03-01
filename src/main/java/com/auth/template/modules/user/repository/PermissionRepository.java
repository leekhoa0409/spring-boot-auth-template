package com.auth.template.modules.user.repository;

import com.auth.template.modules.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, String> {

}
