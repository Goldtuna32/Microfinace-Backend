package com.sme.controller;

import com.sme.dto.CurrentAccountDTO;
import com.sme.service.CurrentAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/current-accounts")
@CrossOrigin(origins = "http://localhost:4200")
public class CurrentAccountController {

    @Autowired
    private CurrentAccountService currentAccountService;

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

    @GetMapping("/exists/{cifId}")
    public boolean hasCurrentAccount(@PathVariable Long cifId) {
        return currentAccountService.hasCurrentAccount(cifId);
    }

    // ✅ Delete Current Account
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentAccount(@PathVariable Long id) {
        currentAccountService.deleteCurrentAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<CurrentAccountDTO>> getAllCurrentAccountsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(currentAccountService.getAllCurrentAccountsPaginated(page, size));
    }
}
