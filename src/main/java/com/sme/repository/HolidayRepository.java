package com.sme.repository;

import com.sme.entity.Branch;
import com.sme.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

     boolean existsByHolidayDate(Date holidayDate);

     long countByHolidayDateBetween(Date startDate, Date endDate);

     List<Holiday> findByHolidayDateBetween(Date startDate, Date endDate);

     @Query("SELECT h FROM Holiday h WHERE YEAR(h.holidayDate) = :year")
     List<Holiday> findByYear(@Param("year") int year);
}
