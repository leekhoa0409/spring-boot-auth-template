package com.auth.template.modules.auth.repository;

import com.auth.template.modules.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {

}
