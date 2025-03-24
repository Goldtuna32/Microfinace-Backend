package com.sme.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Table(name = "permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String name; // e.g., CREATE, READ, UPDATE, DELETE

    @Column(name = "permission_function", nullable = false, length = 45) // Renamed from "function"
    private String permissionFunction; // e.g., CREATE, READ, UPDATE, DELETE

    @Column(nullable = false, length = 255)
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RolePermission> rolePermissions;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}