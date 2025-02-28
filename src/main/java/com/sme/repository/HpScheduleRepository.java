package com.sme.repository;

import com.sme.entity.HpSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HpScheduleRepository extends JpaRepository<HpSchedule, Long> {
    List<HpSchedule> findByHpRegistrationId(Long hpRegistrationId);
}
