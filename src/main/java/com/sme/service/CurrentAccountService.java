package com.sme.service;

import com.sme.dto.CurrentAccountDTO;
import com.sme.entity.CurrentAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


public interface CurrentAccountService {

    List<CurrentAccountDTO> getAllCurrentAccounts();
    Optional<CurrentAccountDTO> getCurrentAccountById(Long id);
    CurrentAccountDTO createCurrentAccount(CurrentAccountDTO accountDTO);
    boolean softDeleteCurrentAccount(Long id);
    boolean restoreCurrentAccount(Long id);

    CurrentAccountDTO updateCurrentAccount(Long id, CurrentAccountDTO accountDTO);

    boolean hasCurrentAccount(Long id);

    List<CurrentAccountDTO> getCurrentAccountsByCifId(Long cifId);

    Page<CurrentAccountDTO> getAllCurrentAccountsPaginated(int page, int size);



}
