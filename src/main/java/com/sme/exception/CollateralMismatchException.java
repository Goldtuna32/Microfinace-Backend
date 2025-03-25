package com.sme.exception;

public class CollateralMismatchException extends SMEException {

    public CollateralMismatchException(Long collateralId, Long cifId) {
        super("LOAN-004", "Collateral ID " + collateralId + " does not belong to CIF ID: " + cifId);
    }
}