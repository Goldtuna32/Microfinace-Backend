package com.sme.service.impl;

import com.sme.dto.*;
import com.sme.entity.*;
import com.sme.exception.BranchCreationException;
import com.sme.exception.BranchNotFoundException;
import com.sme.exception.InvalidBranchCodeException;
import com.sme.repository.AddressRepository;
import com.sme.repository.BranchRepository;
import com.sme.service.BranchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    @PersistenceContext
    private EntityManager entityManager;

    private static final Map<String, String> REGION_CODES = new HashMap<>();

    static {
        REGION_CODES.put("YANGON", "YGN");
        REGION_CODES.put("MANDALAY", "MDY");
        REGION_CODES.put("NAYPYIDAW", "NPT");
        // Add more mappings
    }

    @Override
    @Transactional
    public String getRegionCode(String region) {
        if (region == null || region.trim().isEmpty()) {
            return "UNK";
        }
        String normalizedRegion = region.trim().toUpperCase();
        return REGION_CODES.getOrDefault(normalizedRegion,
                normalizedRegion.substring(0, Math.min(normalizedRegion.length(), 3)));
    }


    @Override
    @Transactional
    public String generateBranchCode(String region) {
        String regionCode = getRegionCode(region);
        String lastBranchCode = branchRepository.findLastBranchCodeByRegion(region);

        if (lastBranchCode == null || lastBranchCode.isEmpty()) {
            return regionCode + "-0001";
        }

        try {
            int prefixLength = regionCode.length() + 1;
            int lastNumber = Integer.parseInt(lastBranchCode.substring(prefixLength));
            int newNumber = lastNumber + 1;
            return regionCode + "-" + String.format("%04d", newNumber);
        } catch (NumberFormatException e) {
            throw new InvalidBranchCodeException(
                    "Failed to parse branch code: " + lastBranchCode, e);
        }
    }

    @Override
    @Transactional
    public BranchDTO createBranch(BranchDTO branchDTO, AddressDTO addressDTO) {
        try {
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
        } catch (Exception e) {
            throw new BranchCreationException(
                    "Failed to create branch with name: " + branchDTO.getBranchName(), e);
        }
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
        if (branch.isEmpty()) {
            throw new BranchNotFoundException(id);
        }
        return branch.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public BranchDTO updateBranch(Long id, BranchDTO branchDTO) {
        Optional<Branch> optionalBranch = branchRepository.findById(id);
        if (!optionalBranch.isPresent()) {
            throw new BranchNotFoundException(id);
        }

        try {
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
        } catch (Exception e) {
            throw new BranchCreationException(
                    "Failed to update branch with id: " + id, e);
        }
    }

    @Override
    @Transactional
    public void deleteBranch(Long id) {
        if (!branchRepository.existsById(id)) {
            throw new BranchNotFoundException(id);
        }
        branchRepository.deleteById(id);
    }

    @Override
    public BranchDTO convertToDTO(Branch branch) {
        return modelMapper.map(branch, BranchDTO.class);
    }


    @Override
    @Transactional
    public Page<BranchDTO> getBranches(Pageable pageable, String region, String name, String branchCode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Branch> cq = cb.createQuery(Branch.class);
        Root<Branch> root = cq.from(Branch.class);

        List<Predicate> predicates = new ArrayList<>();

        if (region != null && !region.isEmpty()) {
            predicates.add(cb.equal(root.get("address").get("region"), region));
        }
        if (name != null && !name.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (branchCode != null && !branchCode.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("branchCode")), "%" + branchCode.toLowerCase() + "%"));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        List<Branch> branches = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        long total = entityManager.createQuery(cq).getResultList().size(); // Note: This is inefficient; see below
        Page<Branch> branchPage = new PageImpl<>(branches, pageable, total);

        return branchPage.map(this::convertToDTO); // Assuming you have a method to convert Branch to BranchDTO
    }

    @Override
    public BranchDetailDTO getBranchDetails(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        BranchDetailDTO dto = mapToBranchDetailDTO(branch);

        // Counts
        dto.setTotalCifs(branchRepository.countCifsByBranchId(branchId));
        dto.setTotalCurrentAccounts(branchRepository.countCurrentAccountsByBranchId(branchId));
        dto.setTotalTransactions(branchRepository.countTransactionsByBranchId(branchId));
        dto.setTotalCollaterals(branchRepository.countCollateralsByBranchId(branchId));
        dto.setTotalDealers(branchRepository.countDealersByBranchId(branchId));
        dto.setOngoingSmeLoans(branchRepository.countOngoingSmeLoansByBranchId(branchId));
        dto.setOngoingHpLoans(branchRepository.countOngoingHpLoansByBranchId(branchId));

        // Top 5 records (already limited by repository)
        dto.setRecentCifs(mapToCifDTOs(branchRepository.findTop5CifsByBranchId(branchId)));
        dto.setRecentCurrentAccounts(mapToCurrentAccountDTOs(branchRepository.findTop5CurrentAccountsByBranchId(branchId)));
        dto.setRecentTransactions(mapToTransactionDTOs(branchRepository.findTop5TransactionsByBranchId(branchId)));
        dto.setRecentCollaterals(mapToCollateralDTOs(branchRepository.findTop5CollateralsByBranchId(branchId)));
        dto.setRecentDealers(mapToDealerDTOs(branchRepository.findTop5DealersByBranchId(branchId)));
        dto.setRecentSmeLoans(mapToSmeLoanDTOs(branchRepository.findTop5OngoingSmeLoansByBranchId(branchId)));
        dto.setRecentHpLoans(mapToHpLoanDTOs(branchRepository.findTop5OngoingHpLoansByBranchId(branchId)));

        return dto;
    }

    private BranchDetailDTO mapToBranchDetailDTO(Branch branch) {
        BranchDetailDTO dto = new BranchDetailDTO();
        dto.setId(branch.getId());
        dto.setBranchName(branch.getName());
        dto.setBranchCode(branch.getBranchCode());
        dto.setPhoneNumber(branch.getPhoneNumber());
        dto.setEmail(branch.getEmail());
        dto.setCreatedDate(branch.getCreatedDate());
        dto.setUpdatedDate(branch.getUpdatedDate());
        dto.setStatus(branch.getStatus());
        if (branch.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setRegion(branch.getAddress().getRegion());
            addressDTO.setDistrict(branch.getAddress().getDistrict());
            addressDTO.setTownship(branch.getAddress().getTownship());
            dto.setAddress(addressDTO);
        }
        return dto;
    }

    private List<CIFDTO> mapToCifDTOs(List<CIF> cifs) {
        return cifs.stream().map(cif -> {
            CIFDTO dto = new CIFDTO();
            dto.setId(cif.getId());
            dto.setName(cif.getName());
            dto.setNrcNumber(cif.getNrcNumber());
            dto.setPhoneNumber(cif.getPhoneNumber());
            dto.setSerialNumber(cif.getSerialNumber());
            dto.setStatus(cif.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<CurrentAccountDTO> mapToCurrentAccountDTOs(List<CurrentAccount> accounts) {
        return accounts.stream().map(account -> {
            CurrentAccountDTO dto = new CurrentAccountDTO();
            dto.setId(account.getId());
            dto.setAccountNumber(account.getAccountNumber());
            dto.setBalance(account.getBalance());
            dto.setStatus(account.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<AccountTransactionDTO> mapToTransactionDTOs(List<AccountTransaction> transactions) {
        return transactions.stream().map(transaction -> {
            AccountTransactionDTO dto = new AccountTransactionDTO();
            dto.setId(transaction.getId());
            dto.setTransactionType(transaction.getTransactionType());
            dto.setAmount(new java.math.BigDecimal(transaction.getAmount()));
            dto.setTransactionDate(transaction.getTransactionDate());
            dto.setTransactionDescription(transaction.getTransactionDescription());
            dto.setStatus(transaction.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<CollateralDTO> mapToCollateralDTOs(List<Collateral> collaterals) {
        return collaterals.stream().map(collateral -> {
            CollateralDTO dto = new CollateralDTO();
            dto.setId(collateral.getId());
            dto.setDescription(collateral.getDescription());
            dto.setValue(collateral.getValue());
            dto.setStatus(collateral.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<DealerRegistrationDTO> mapToDealerDTOs(List<DealerRegistration> dealers) {
        return dealers.stream().map(dealer -> {
            DealerRegistrationDTO dto = new DealerRegistrationDTO();
            dto.setId(dealer.getId());
            dto.setCompanyName(dealer.getCompanyName());
            dto.setPhoneNumber(dealer.getPhoneNumber());
            dto.setRegistrationDate(dealer.getRegistrationDate());
            dto.setStatus(dealer.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<SmeLoanRegistrationDTO> mapToSmeLoanDTOs(List<SmeLoanRegistration> loans) {
        return loans.stream().map(loan -> {
            SmeLoanRegistrationDTO dto = new SmeLoanRegistrationDTO();
            dto.setId(loan.getId());
            dto.setSerialCode(loan.getSerialCode());
            dto.setLoanAmount(loan.getLoanAmount());
            dto.setStatus(loan.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<HpRegistrationDTO> mapToHpLoanDTOs(List<HpRegistration> loans) {
        return loans.stream().map(loan -> {
            HpRegistrationDTO dto = new HpRegistrationDTO();
            dto.setId(loan.getId());
            dto.setHpNumber(loan.getHpNumber());
            dto.setLoanAmount(loan.getLoanAmount());
            dto.setStatus(loan.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }
}
