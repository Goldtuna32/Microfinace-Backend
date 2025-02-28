package com.sme.repository;

import com.sme.entity.CollateralType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollateralTypeRepository extends JpaRepository<CollateralType, Long> {

}
