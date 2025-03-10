package com.sme.repository;

 
import com.sme.entity.DealerRegistration;
import com.sme.entity.HpProduct;
import com.sme.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HpProductRepository extends JpaRepository<HpProduct, Long> {

    // ✅ Find active products
    List<HpProduct> findByStatus(Integer status);

    @Query("SELECT h FROM HpProduct h WHERE h.status != 0")
    List<HpProduct> findAllActive();

    @Modifying
    @Query("UPDATE HpProduct h SET h.status = 2 WHERE h.id = :id")
    void softDelete(@Param("id") Long id);

    List<HpProduct> findByProductType(ProductType productType);

    List<HpProduct> findByDealerRegistration(DealerRegistration dealerRegistration);


}
