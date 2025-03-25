package com.sme.service;

import com.sme.dto.CollateralDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface CollateralService {

    List<CollateralDTO> getAllCollaterals();
    List<CollateralDTO> getDeletedCollaterals();

    Optional<CollateralDTO> getCollateralById(Long id);

    CollateralDTO createCollateral(CollateralDTO collateralDTO, MultipartFile frontPhoto, MultipartFile backPhoto) throws IOException;
    CollateralDTO updateCollateral(Long id, CollateralDTO collateralDTO, MultipartFile frontPhoto, MultipartFile backPhoto) throws IOException;
    boolean softDeleteCollateral(Long id);  // Renamed to clarify it’s a soft delete
    boolean restoreCollateral(Long id);

    Page<CollateralDTO> getAllCollateralsPaginated(Pageable pageable);

    List<CollateralDTO> getCollateralsByCifId(Long cifId);

}
