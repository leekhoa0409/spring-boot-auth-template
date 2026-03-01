package com.auth.template.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permissions")
@Builder
public class Permission {
    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String permission;

    @Column(nullable = false, unique = true, length = 50)
    private String description;
}
