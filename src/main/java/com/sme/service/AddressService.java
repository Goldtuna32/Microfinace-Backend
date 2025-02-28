package com.sme.service;

import com.sme.entity.Address;
import com.sme.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface AddressService {

    List<Address> getAllAddresses();

    Optional<Address> getAddressById(Long id);

    Address createAddress(Address address);

    Address updateAddress(Long id, Address addressDetails);

    void deleteAddress(Long id);
}
