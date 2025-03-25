package com.sme.controller;

import com.sme.dto.CurrentAccountDTO;
import com.sme.entity.CurrentAccount;
import com.sme.repository.CurrentAccountRepository;
import com.sme.service.CurrentAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/current-accounts")
@CrossOrigin(origins = "http://localhost:4200")
public class CurrentAccountController {

    @Autowired
    private CurrentAccountService currentAccountService;

    @Autowired
    private CurrentAccountRepository currentAccountRepository;

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_READ')")
    @GetMapping
    public List<CurrentAccountDTO> getAllCurrentAccounts() {
        return currentAccountService.getAllCurrentAccounts();
    }

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_READ')")
    @GetMapping("/{id}")
    public ResponseEntity<CurrentAccountDTO> getCurrentAccountById(@PathVariable Long id) {
        Optional<CurrentAccountDTO> account = currentAccountService.getCurrentAccountById(id);
        return account.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_CREATE')")
    @PostMapping
    public ResponseEntity<?> createCurrentAccount(@RequestBody CurrentAccountDTO accountDTO) {
        try {
            return ResponseEntity.ok(currentAccountService.createCurrentAccount(accountDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCurrentAccount(@PathVariable Long id, @RequestBody CurrentAccountDTO accountDTO) {
        try {
            CurrentAccountDTO updatedAccount = currentAccountService.updateCurrentAccount(id, accountDTO);
            return ResponseEntity.ok(updatedAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_READ')")
    @GetMapping("/exists/{cifId}")
    public boolean hasCurrentAccount(@PathVariable Long cifId) {
        return currentAccountService.hasCurrentAccount(cifId);
    }

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentAccount(@PathVariable Long id) {
        currentAccountService.softDeleteCurrentAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_READ')")
    @GetMapping("/paginated")
    public ResponseEntity<Page<CurrentAccountDTO>> getAllCurrentAccountsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(currentAccountService.getAllCurrentAccountsPaginated(page, size));
    }

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_READ')")
    @GetMapping("/by-cif/{cifId}")
    public CurrentAccountDTO getCurrentAccountsByCifId(@PathVariable Long cifId) {
        return currentAccountService.getCurrentAccountByCifId(cifId);
    }

    @PreAuthorize("hasRole('CURRENT_ACCOUNT_READ')")
    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<List<CurrentAccountDTO>> getCurrentAccountsByCifSerialNumber(@PathVariable String serialNumber) {
        List<CurrentAccount> accounts = currentAccountRepository.findByCifSerialNumber(serialNumber);
        List<CurrentAccountDTO> dtos = accounts.stream()
                .map(account -> new CurrentAccountDTO(
                        account.getId(),
                        account.getAccountNumber(),
                        account.getCif() != null ? account.getCif().getId() : null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
