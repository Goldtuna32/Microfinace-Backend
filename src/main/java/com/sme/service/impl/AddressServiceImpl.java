package com.sme.service.impl;

import com.sme.entity.Address;
import com.sme.exception.AddressCreationException;
import com.sme.exception.AddressNotFoundException;
import com.sme.exception.AddressUpdateException;
import com.sme.repository.AddressRepository;
import com.sme.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    @Override
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }

    @Override
    public Address createAddress(Address address) {
        try {
            return addressRepository.save(address);
        } catch (Exception e) {
            throw new AddressCreationException(
                    "Failed to create address with region: " + address.getRegion(), e);
        }
    }

    @Override
    public Address updateAddress(Long id, Address addressDetails) {
        Optional<Address> optionalAddress = addressRepository.findById(id);

        if (!optionalAddress.isPresent()) {
            throw new AddressNotFoundException(id);
        }

        try {
            Address address = optionalAddress.get();
            address.setRegion(addressDetails.getRegion());
            address.setDistrict(addressDetails.getDistrict());
            address.setTownship(addressDetails.getTownship());
            address.setStreet(addressDetails.getStreet());
            return addressRepository.save(address);
        } catch (Exception e) {
            throw new AddressUpdateException(
                    "Failed to update address with id: " + id, e);
        }
    }

    @Override
    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
}
