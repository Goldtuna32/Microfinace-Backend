package com.sme.repository;

import com.sme.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query("SELECT b.branchCode FROM Branch b ORDER BY b.branchCode DESC LIMIT 1")
    String findLastBranchCode();

    @Query("SELECT b.branchCode FROM Branch b " +
            "JOIN b.address a " +
            "WHERE a.region = :region AND TRIM(a.township) = :township " +
            "ORDER BY b.id DESC")
    String findLastBranchCodeByRegionAndTownship(@Param("region") String region,
                                                 @Param("township") String township);

    @Query("SELECT b FROM Branch b " +
            "WHERE (:region IS NULL OR b.address.region = :region) " +
            "AND (:name IS NULL OR LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:branchCode IS NULL OR LOWER(b.branchCode) LIKE LOWER(CONCAT('%', :branchCode, '%')))")
    Page<Branch> findBranches(
            @Param("region") String region,
            @Param("name") String name,
            @Param("branchCode") String branchCode,
            Pageable pageable);


    @Query("SELECT b.branchCode FROM Branch b WHERE b.address.region = :region ORDER BY b.branchCode DESC LIMIT 1")
    String findLastBranchCodeByRegion(@Param("region") String region);

    boolean existsByName(String Name);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    @Query("SELECT COUNT(c) FROM CIF c WHERE c.branch.id = :branchId")
    Long countCifsByBranchId(Long branchId);

    // CIFs: Select specific columns to avoid duplicate 'id'
    @Query(value = "SELECT c.id AS cif_id, c.serial_number, c.name, c.nrc_number, c.dob, c.gender, c.phone_number, " +
            "c.email, c.address, c.martial_status, c.occupation, c.income_source, c.status, c.created_at, " +
            "c.f_nrc_photo_url, c.b_nrc_photo_url, c.branch_id " +
            "FROM cif c WHERE c.branch_id = :branchId ORDER BY c.created_at DESC LIMIT 5", nativeQuery = true)
    List<CIF> findTop5CifsByBranchId(Long branchId);

    @Query("SELECT COUNT(ca) FROM CurrentAccount ca WHERE ca.cif.branch.id = :branchId")
    Long countCurrentAccountsByBranchId(Long branchId);

    // Current Accounts: Explicit columns with aliases
    @Query(value = "SELECT ca.id AS ca_id, ca.account_number, ca.balance, ca.maximium_balance, ca.minimum_balance, " +
            "ca.account_status, ca.date_created, ca.hold_amount, ca.cif_id " +
            "FROM current_account ca JOIN cif c ON ca.cif_id = c.id " +
            "WHERE c.branch_id = :branchId ORDER BY ca.date_created DESC LIMIT 5", nativeQuery = true)
    List<CurrentAccount> findTop5CurrentAccountsByBranchId(Long branchId);

    @Query("SELECT COUNT(t) FROM AccountTransaction t WHERE t.currentAccount.cif.branch.id = :branchId")
    Long countTransactionsByBranchId(Long branchId);

    // Transactions: Explicit columns with aliases
    @Query(value = "SELECT t.id AS t_id, t.transaction_type, t.amount, t.transaction_date, t.account_transaction_desc, " +
            "t.transaction_status, t.current_account_id " +
            "FROM account_transaction t " +
            "JOIN current_account ca ON t.current_account_id = ca.id " +
            "JOIN cif c ON ca.cif_id = c.id " +
            "WHERE c.branch_id = :branchId ORDER BY t.transaction_date DESC LIMIT 5", nativeQuery = true)
    List<AccountTransaction> findTop5TransactionsByBranchId(Long branchId);

    @Query("SELECT COUNT(col) FROM Collateral col WHERE col.cif.branch.id = :branchId")
    Long countCollateralsByBranchId(Long branchId);

    // Collaterals: Explicit columns with aliases
    @Query(value = "SELECT col.id AS col_id, col.value, col.description, col.f_collateral_photo, col.b_collateral_photo, " +
            "col.status, col.date, col.collateral_code, col.cif_id, col.collateral_type_id " +
            "FROM collateral col JOIN cif c ON col.cif_id = c.id " +
            "WHERE c.branch_id = :branchId ORDER BY col.date DESC LIMIT 5", nativeQuery = true)
    List<Collateral> findTop5CollateralsByBranchId(Long branchId);

    @Query("SELECT COUNT(d) FROM DealerRegistration d WHERE d.address.branch.id = :branchId")
    Long countDealersByBranchId(Long branchId);

    // Dealers: Explicit columns with aliases
    @Query(value = "SELECT d.id AS d_id, d.company_name, d.phone_number, d.registration_date, d.status, " +
            "d.address_id, d.current_account_id, d.deleted " +
            "FROM dealer_registration d JOIN address a ON d.address_id = a.id " +
            "WHERE a.branch_id = :branchId ORDER BY d.registration_date DESC LIMIT 5", nativeQuery = true)
    List<DealerRegistration> findTop5DealersByBranchId(Long branchId);

    // SME Loans: Fixed join (lr.cif, not lr.currentAccount)
    @Query("SELECT COUNT(lr) FROM SmeLoanRegistration lr WHERE lr.currentAccount.cif.branch.id = :branchId AND lr.status = 1")
    Long countOngoingSmeLoansByBranchId(Long branchId);

    // SME Loans: Explicit columns with aliases
    @Query(value = "SELECT lr.id AS lr_id, lr.serial_code, lr.loan_amount, lr.status, lr.cif_id, lr.due_date " +
            "FROM sme_loan_registration lr JOIN cif c ON lr.cif_id = c.id " +
            "WHERE c.branch_id = :branchId AND lr.status = 1 ORDER BY lr.due_date DESC LIMIT 5", nativeQuery = true)
    List<SmeLoanRegistration> findTop5OngoingSmeLoansByBranchId(Long branchId);

    @Query("SELECT COUNT(hr) FROM HpRegistration hr WHERE hr.currentAccount.cif.branch.id = :branchId AND hr.status = 1")
    Long countOngoingHpLoansByBranchId(Long branchId);

    // HP Loans: Explicit columns with aliases
    @Query(value = "SELECT hr.id AS hr_id, hr.hp_number, hr.created_date, hr.grace_period, hr.loan_amount, " +
            "hr.down_payment, hr.loan_term, hr.interest_rate, hr.late_fee_rate, hr.ninety_day_late_fee_rate, " +
            "hr.one_hundred_and_eighty_day_late_fee_rate, hr.start_date, hr.status, hr.current_account_id, " +
            "hr.hp_product_id " +
            "FROM hp_registration hr JOIN current_account ca ON hr.current_account_id = ca.id " +
            "JOIN cif c ON ca.cif_id = c.id " +
            "WHERE c.branch_id = :branchId AND hr.status = 1 ORDER BY hr.created_date DESC LIMIT 5", nativeQuery = true)
    List<HpRegistration> findTop5OngoingHpLoansByBranchId(Long branchId);

}
