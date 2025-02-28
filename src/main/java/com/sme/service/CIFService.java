package com.sme.service;

import com.sme.dto.CIFDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CIFService {

    List<CIFDTO> getAllCIFs();

    Optional<CIFDTO> getCIFById(Long id);

    CIFDTO createCIF(CIFDTO cifDTO, MultipartFile frontNrc, MultipartFile backNrc) throws IOException;

    CIFDTO updateCIF(Long id, CIFDTO cifDTO,MultipartFile frontNrc, MultipartFile backNrc) throws IOException;

    void deleteCIF(Long id);
}