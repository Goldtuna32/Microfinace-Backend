package com.sme.config;

import com.sme.entity.Permission;
import com.sme.repository.PermissionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

// PermissionInitializer.java
@Component
public class PermissionInitializer {

    @Autowired
    private PermissionRepository permissionRepository;

    @PostConstruct
    public void init() {
         List<Map<String, String>> permissions = Arrays.asList(
                // BRANCH permissions
                Map.of("function", "BRANCH", "name", "CREATE", "description", "Create branches"),
                Map.of("function", "BRANCH", "name", "READ", "description", "Read branches"),
                Map.of("function", "BRANCH", "name", "UPDATE", "description", "Update branches"),
                Map.of("function", "BRANCH", "name", "DELETE", "description", "Delete branches"),
                // CIF permissions
                Map.of("function", "CIF", "name", "CREATE", "description", "Create CIF records"),
                Map.of("function", "CIF", "name", "READ", "description", "Read CIF records"),
                Map.of("function", "CIF", "name", "UPDATE", "description", "Update CIF records"),
                Map.of("function", "CIF", "name", "DELETE", "description", "Delete CIF records"),
                // CURRENT_ACCOUNT permissions
                Map.of("function", "CURRENT_ACCOUNT", "name", "CREATE", "description", "Create current accounts"),
                Map.of("function", "CURRENT_ACCOUNT", "name", "READ", "description", "Read current accounts"),
                Map.of("function", "CURRENT_ACCOUNT", "name", "UPDATE", "description", "Update current accounts"),
                Map.of("function", "CURRENT_ACCOUNT", "name", "DELETE", "description", "Delete current accounts"),
                // TRANSACTION permissions
                Map.of("function", "TRANSACTION", "name", "CREATE", "description", "Create transactions"),
                Map.of("function", "TRANSACTION", "name", "READ", "description", "Read transactions"),
                Map.of("function", "TRANSACTION", "name", "UPDATE", "description", "Update transactions"),
                Map.of("function", "TRANSACTION", "name", "DELETE", "description", "Delete transactions"),
                // COLLATERAL_TYPE permissions
                Map.of("function", "COLLATERAL_TYPE", "name", "CREATE", "description", "Create collateral types"),
                Map.of("function", "COLLATERAL_TYPE", "name", "READ", "description", "Read collateral types"),
                Map.of("function", "COLLATERAL_TYPE", "name", "UPDATE", "description", "Update collateral types"),
                Map.of("function", "COLLATERAL_TYPE", "name", "DELETE", "description", "Delete collateral types"),
                // COLLATERAL permissions
                Map.of("function", "COLLATERAL", "name", "CREATE", "description", "Create collaterals"),
                Map.of("function", "COLLATERAL", "name", "READ", "description", "Read collaterals"),
                Map.of("function", "COLLATERAL", "name", "UPDATE", "description", "Update collaterals"),
                Map.of("function", "COLLATERAL", "name", "DELETE", "description", "Delete collaterals"),
                // LOAN permissions
                Map.of("function", "LOAN", "name", "CREATE", "description", "Create loans"),
                Map.of("function", "LOAN", "name", "READ", "description", "Read loans"),
                Map.of("function", "LOAN", "name", "UPDATE", "description", "Update loans"),
                Map.of("function", "LOAN", "name", "DELETE", "description", "Delete loans"),
                // DEALER permissions
                Map.of("function", "DEALER", "name", "CREATE", "description", "Create dealers"),
                Map.of("function", "DEALER", "name", "READ", "description", "Read dealers"),
                Map.of("function", "DEALER", "name", "UPDATE", "description", "Update dealers"),
                Map.of("function", "DEALER", "name", "DELETE", "description", "Delete dealers"),
                // PRODUCT_TYPE permissions
                Map.of("function", "PRODUCT_TYPE", "name", "CREATE", "description", "Create product types"),
                Map.of("function", "PRODUCT_TYPE", "name", "READ", "description", "Read product types"),
                Map.of("function", "PRODUCT_TYPE", "name", "UPDATE", "description", "Update product types"),
                Map.of("function", "PRODUCT_TYPE", "name", "DELETE", "description", "Delete product types"),
                // HP_PRODUCT permissions
                Map.of("function", "HP_PRODUCT", "name", "CREATE", "description", "Create HP products"),
                Map.of("function", "HP_PRODUCT", "name", "READ", "description", "Read HP products"),
                Map.of("function", "HP_PRODUCT", "name", "UPDATE", "description", "Update HP products"),
                Map.of("function", "HP_PRODUCT", "name", "DELETE", "description", "Delete HP products"),
                // ROLE permission (for creating roles)
                Map.of("function", "ROLE", "name", "CREATE", "description", "Create roles")
        );

        // Check if the permission table is empty and populate it
        if (permissionRepository.count() == 0) {
            for (Map<String, String> perm : permissions) {
                Permission permission = new Permission();
                permission.setPermissionFunction(perm.get("function"));
                permission.setName(perm.get("name"));
                permission.setDescription(perm.get("description"));
                permission.setCreatedAt(new Date());
                permissionRepository.save(permission);
            }
        }
    }
}