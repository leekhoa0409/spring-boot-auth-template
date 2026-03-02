package com.auth.template.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "roles")
@Builder
public class Role {
    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String roleName;

    @Column(nullable = false, unique = true, length = 50)
    private String description;

    @ManyToMany
    Set<Permission> permissions;
}
