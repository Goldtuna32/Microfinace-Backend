package com.sme.repository;

import com.sme.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    public Address findByRegionAndDistrictAndTownshipAndStreet(String region,String district, String township,String street);

}
