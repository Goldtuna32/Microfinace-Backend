package com.sme.service.impl;

import com.sme.dto.AddressDTO;
import com.sme.dto.BranchDTO;
import com.sme.entity.Address;
import com.sme.entity.Branch;
import com.sme.repository.AddressRepository;
import com.sme.repository.BranchRepository;
import com.sme.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchServiceImpl implements BranchService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    @Transactional
    public String getRegionCode(String region) {
        if (region == null || region.trim().isEmpty()) {
            return "UNK";
        }


        return region.trim().substring(0, Math.min(region.length(), 3)).toUpperCase();
    }


    @Override
    @Transactional
    public String getTownshipCode(String township) {
        return township.length() >= 3 ? township.substring(0, 3).toUpperCase() : township.toUpperCase();
    }

    @Override
    @Transactional
    public String generateBranchCode(String region) {
        String regionCode = getRegionCode(region);
        String lastBranchCode = branchRepository.findLastBranchCodeByRegion(region); // Updated repository method

        if (lastBranchCode == null || lastBranchCode.isEmpty()) {
            return regionCode + "-0001";
        }

        try {
            // Extract the numeric part after the region code and hyphen
            // Format: "{regionCode}-{number}", so skip regionCode length + 1 for the hyphen
            int prefixLength = regionCode.length() + 1;
            int lastNumber = Integer.parseInt(lastBranchCode.substring(prefixLength));
            int newNumber = lastNumber + 1;

            // Format as a 4-digit string
            return regionCode + "-" + String.format("%04d", newNumber);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing branch code: " + lastBranchCode);
            return regionCode + "-0001"; // Fallback to "0001" on error
        }
    }

    @Override
    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO, AddressDTO addressDTO) {
        Address address = modelMapper.map(addressDTO, Address.class);
        addressRepository.save(address);

        Branch branch = modelMapper.map(branchDTO, Branch.class);
        branch.setAddress(address);
        branch.setStatus(1);
        branch.setCreatedDate(new Date());
        branch.setUpdatedDate(new Date());
        branch.setBranchCode(generateBranchCode(addressDTO.getRegion()));

        Branch savedBranch = branchRepository.save(branch);

        return modelMapper.map(savedBranch, BranchDTO.class);
    }

    @Override
    @Transactional
    public List<BranchDTO> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();
        return branches.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<BranchDTO> getBranchById(Long id) {
        Optional<Branch> branch = branchRepository.findById(id);
        return branch.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public BranchDTO updateBranch(Long id, BranchDTO branchDTO) {
        Optional<Branch> optionalBranch = branchRepository.findById(id);
        if (optionalBranch.isPresent()) {
            Branch branch = optionalBranch.get();
            branch.setName(branchDTO.getBranchName());
            branch.setBranchCode(branchDTO.getBranchCode());
            branch.setPhoneNumber(branchDTO.getPhoneNumber());
            branch.setEmail(branchDTO.getEmail());
            branch.setStatus(branchDTO.getStatus());
            branch.setCreatedDate(branchDTO.getCreatedDate());
            branch.setUpdatedDate(branchDTO.getUpdatedDate());

            if (branchDTO.getAddress() != null) {
                AddressDTO addressDTO = branchDTO.getAddress();
                Address address = new Address();
                address.setId(addressDTO.getId());
                address.setDistrict(addressDTO.getDistrict());
                address.setStreet(addressDTO.getStreet());

                branch.setAddress(address);
            }

            Branch updatedBranch = branchRepository.save(branch);
            return convertToDTO(updatedBranch);
        } else {
            throw new RuntimeException("Branch not found with id: " + id);
        }
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        branchRepository.deleteById(id);
    }

    @Override
    public BranchDTO convertToDTO(Branch branch) {
        return modelMapper.map(branch, BranchDTO.class);
    }


    @Override
    @Transactional
    public Page<BranchDTO> getBranches(Pageable pageable) {
        Page<Branch> branchPage = branchRepository.findAll(pageable);
        return branchPage.map(this::convertToDTO);
    }
}
