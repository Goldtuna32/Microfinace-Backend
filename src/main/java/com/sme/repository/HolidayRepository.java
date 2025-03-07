package com.sme.repository;

import com.sme.entity.Branch;
import com.sme.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

     List<Holiday> findByBranchId(Long branchId);

     boolean existsByBranchAndHolidayDate(Branch branch, Date holidayDate);

     boolean existsByHolidayDate(LocalDate date);

     List<Holiday> findByHolidayDate(Date holidayDate);

}
