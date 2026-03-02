package com.auth.template.config;

import com.auth.template.modules.auth.entity.Permission;
import com.auth.template.modules.auth.entity.Role;
import com.auth.template.modules.user.entity.User;
import com.auth.template.modules.auth.repository.PermissionRepository;
import com.auth.template.modules.auth.repository.RoleRepository;
import com.auth.template.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ApplicationInitConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final String ADMIN_USERNAME = "admin";
    private final String ADMIN_PASSWORD = "admin@123";
    private final String ADMIN_EMAIL = "admin@exmaple.com";

    @Bean
    ApplicationRunner applicationRunner(PermissionRepository permissionRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USERNAME).isEmpty()) {
                roleRepository.save(Role.builder()
                        .roleName("USER")
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .roleName("ADMIN")
                        .description("Admin role")
                        .build());

                HashSet<Role> roles = new HashSet<>();
                roles.add(adminRole);

                HashSet<Permission> permissions = new HashSet<>();
                Permission allPermission = permissionRepository.save(Permission.builder()
                        .permission("ALL")
                        .description("All permissions")
                        .build());

                Permission readPermission = permissionRepository.save(Permission.builder()
                        .permission("READ")
                        .description("Read permission")
                        .build());

                Permission writePermission = permissionRepository.save(Permission.builder()
                        .permission("WRITE")
                        .description("Write permission")
                        .build());

                Permission deletePermission = permissionRepository.save(Permission.builder()
                        .permission("DELETE")
                        .description("Delete permission")
                        .build());

                permissions.add(allPermission);
                adminRole.setPermissions(permissions);
                roleRepository.save(adminRole);

                User initUser = userRepository.save(User.builder()
                        .username(ADMIN_USERNAME)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .email(ADMIN_EMAIL)
                        .roles(roles)
                        .build());

                userRepository.save(initUser);
                log.warn("Admin user has been created with default password: admin !!!");
            }
            log.info("Application initialization completed !!!");
        };
    }
}
