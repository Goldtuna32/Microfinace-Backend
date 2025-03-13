package com.sme.controller;

import com.sme.dto.CurrentAccountDTO;
import com.sme.entity.CurrentAccount;
import com.sme.repository.CurrentAccountRepository;
import com.sme.service.CurrentAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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

    // ✅ Get all Current Accounts
    @GetMapping
    public List<CurrentAccountDTO> getAllCurrentAccounts() {
        return currentAccountService.getAllCurrentAccounts();
    }

    // ✅ Get Current Account by ID
    @GetMapping("/{id}")
    public ResponseEntity<CurrentAccountDTO> getCurrentAccountById(@PathVariable Long id) {
        Optional<CurrentAccountDTO> account = currentAccountService.getCurrentAccountById(id);
        return account.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Create a new Current Account
    @PostMapping
    public ResponseEntity<?> createCurrentAccount(@RequestBody CurrentAccountDTO accountDTO) {
        try {
            return ResponseEntity.ok(currentAccountService.createCurrentAccount(accountDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCurrentAccount(@PathVariable Long id, @RequestBody CurrentAccountDTO accountDTO) {
        try {
            CurrentAccountDTO updatedAccount = currentAccountService.updateCurrentAccount(id, accountDTO);
            return ResponseEntity.ok(updatedAccount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exists/{cifId}")
    public boolean hasCurrentAccount(@PathVariable Long cifId) {
        return currentAccountService.hasCurrentAccount(cifId);
    }

    // ✅ Delete Current Account
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentAccount(@PathVariable Long id) {
        currentAccountService.softDeleteCurrentAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<CurrentAccountDTO>> getAllCurrentAccountsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(currentAccountService.getAllCurrentAccountsPaginated(page, size));
    }

    @GetMapping("/by-cif/{cifId}")
    public List<CurrentAccountDTO> getCurrentAccountsByCifId(@PathVariable Long cifId) {
        return currentAccountService.getCurrentAccountsByCifId(cifId);
    }

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
