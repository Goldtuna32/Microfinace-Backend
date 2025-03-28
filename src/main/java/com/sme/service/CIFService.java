package com.sme.service;

import com.sme.dto.CIFDTO;
import com.sme.exception.CIFNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CIFService {

    Page<CIFDTO> getDeletedCIFs(Pageable pageable, String nrcPrefix);
    Page<CIFDTO> getAllCIFs(Pageable pageable, String nrcPrefix);

    List<CIFDTO> getDeletedCIFS();
    List<CIFDTO> getAllCifs();

    CIFDTO findCifByCurrentAccountId(Long currentAccountId);

    Optional<CIFDTO> getCIFById(Long id);

    CIFDTO createCIF(CIFDTO cifDTO, MultipartFile frontNrc, MultipartFile backNrc) throws IOException;

    CIFDTO updateCIF(Long id, CIFDTO cifDTO,MultipartFile frontNrc, MultipartFile backNrc) throws IOException;

    boolean softDeleteCIF(Long id); // Soft delete (set status to 2)
    boolean restoreCIF(Long id);

    CIFDTO getCifById(Long cifId) throws CIFNotFoundException;

    long getTotalCifCount();
    long getActiveCifCount();

    Page<CIFDTO> getDeletedCIFsByBranch(Pageable pageable, Long branchId, String nrcPrefix);

    Page<CIFDTO> getAllCIFsByBranch(Pageable pageable, Long branchId, String nrcPrefix);
}