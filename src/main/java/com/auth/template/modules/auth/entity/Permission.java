package com.auth.template.modules.auth.entity;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String permission;

    @Column(nullable = false, unique = true, length = 50)
    private String description;
}
