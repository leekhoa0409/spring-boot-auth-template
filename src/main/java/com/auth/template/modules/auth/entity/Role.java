package com.auth.template.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @Column(nullable = false, unique = true, length = 20)
    private String roleName;

    @Column(nullable = false, unique = true, length = 50)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    Set<Permission> permissions;
}
