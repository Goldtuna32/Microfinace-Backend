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

    Optional<CollateralDTO> getCollateralById(Long id);

    CollateralDTO createCollateral(CollateralDTO collateralDTO, MultipartFile frontPhoto, MultipartFile backPhoto) throws IOException;
    Optional<CollateralDTO> updateCollateral(Long id, CollateralDTO collateralDTO);
    boolean deleteCollateral(Long id);

    Page<CollateralDTO> getAllCollateralsPaginated(Pageable pageable);

}
