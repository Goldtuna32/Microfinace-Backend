package com.sme.repository;

 
import com.sme.entity.DealerRegistration;
import com.sme.entity.HpProduct;
import com.sme.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HpProductRepository extends JpaRepository<HpProduct, Long> {

    // ✅ Find active products
    List<HpProduct> findByStatus(Integer status);


    List<HpProduct> findByProductType(ProductType productType);

    List<HpProduct> findByDealerRegistration(DealerRegistration dealerRegistration);


}
