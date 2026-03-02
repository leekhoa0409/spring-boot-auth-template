package com.auth.template.modules.auth.repository;

import com.auth.template.modules.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

}
