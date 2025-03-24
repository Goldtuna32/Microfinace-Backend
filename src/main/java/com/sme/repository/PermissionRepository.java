package com.sme.repository;

import com.sme.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByPermissionFunctionAndName(String permissionFunction, String name);
    Optional<Permission> findByName(String name);

}