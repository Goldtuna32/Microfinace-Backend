package com.sme.repository;

import com.sme.entity.HpSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HpScheduleRepository extends JpaRepository<HpSchedule, Long> {
    List<HpSchedule> findByHpRegistrationId(Long hpRegistrationId);

    @Query("SELECT h FROM HpSchedule h WHERE " +
           "(h.dueDate = :today OR " +
           "(h.dueDate < :today AND h.graceEndDate >= :today) OR " +
           "h.graceEndDate < :today) AND " +
           "h.status != 6")
    List<HpSchedule> findSchedulesForProcessing(@Param("today") LocalDate today);
}
