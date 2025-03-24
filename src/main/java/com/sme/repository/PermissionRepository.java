package com.sme.repository;

import com.sme.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
//    List<Permission> findByFunction(String function);

    Optional<Permission> findBypermissionFunctionAndName(String permissionFunction, String name);

}