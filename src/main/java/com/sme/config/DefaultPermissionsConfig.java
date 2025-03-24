package com.sme.config;

import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DefaultPermissionsConfig {

    public static final Map<String, List<String>> DEFAULT_PERMISSIONS = new HashMap<>();

    static {
        // ADMIN gets all permissions (you can list all explicitly or handle dynamically)
        DEFAULT_PERMISSIONS.put("ADMIN", Arrays.asList(
                "DASHBOARD_READ",
                "BRANCH_CREATE", "BRANCH_READ", "BRANCH_UPDATE", "BRANCH_DELETE",
                "CIF_CREATE", "CIF_READ", "CIF_UPDATE", "CIF_DELETE",
                "CURRENT_ACCOUNT_CREATE", "CURRENT_ACCOUNT_READ", "CURRENT_ACCOUNT_UPDATE", "CURRENT_ACCOUNT_DELETE",
                "TRANSACTION_CREATE", "TRANSACTION_READ", "TRANSACTION_UPDATE", "TRANSACTION_DELETE",
                "COLLATERAL_TYPE_CREATE", "COLLATERAL_TYPE_READ", "COLLATERAL_TYPE_UPDATE", "COLLATERAL_TYPE_DELETE",
                "COLLATERAL_CREATE", "COLLATERAL_READ", "COLLATERAL_UPDATE", "COLLATERAL_DELETE",
                "LOAN_CREATE", "LOAN_READ", "LOAN_UPDATE", "LOAN_DELETE",
                "DEALER_CREATE", "DEALER_READ", "DEALER_UPDATE", "DEALER_DELETE",
                "PRODUCT_TYPE_CREATE", "PRODUCT_TYPE_READ", "PRODUCT_TYPE_UPDATE", "PRODUCT_TYPE_DELETE",
                "HP_PRODUCT_CREATE", "HP_PRODUCT_READ", "HP_PRODUCT_UPDATE", "HP_PRODUCT_DELETE",
                "UI_COMPONENT_READ", "AUTH_SIGNUP", "AUTH_SIGNIN", "CHART_READ",
                "FORMS_READ", "TABLES_READ", "SAMPLE_PAGE_READ"
        ));

        // MANAGER gets specific permissions
        DEFAULT_PERMISSIONS.put("MANAGER", Arrays.asList(
                "CIF_CREATE", "CIF_READ", "CIF_UPDATE", "CIF_DELETE",
                "CURRENT_ACCOUNT_CREATE", "CURRENT_ACCOUNT_READ", "CURRENT_ACCOUNT_UPDATE", "CURRENT_ACCOUNT_DELETE"
        ));

        // Add more roles as needed, e.g., USER
        DEFAULT_PERMISSIONS.put("USER", Arrays.asList(
                "CIF_READ", "CURRENT_ACCOUNT_READ"
        ));
    }

    public List<String> getDefaultPermissionsForRole(String roleName) {
        return DEFAULT_PERMISSIONS.getOrDefault(roleName, Arrays.asList());
    }
}