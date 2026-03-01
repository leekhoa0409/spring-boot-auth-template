package com.auth.template.modules.user.repository;

import com.auth.template.modules.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, String> {

}
