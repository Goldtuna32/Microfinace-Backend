package com.sme.repository;

import com.sme.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    List<RolePermission> findByRoleId(Long roleId);
//    List<RolePermission> findByPermissionId(Long permissionId);
//    void deleteByRoleId(Long roleId);
//    void deleteByPermissionId(Long permissionId);
}
