package com.sme.service.impl;

import com.sme.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayScheduler {
    private final HolidayService holidayService;

    @Scheduled(cron = "0 5 0 1 1 *")
    public void importHolidaysForNewYear() {
        log.info("⏰ Scheduled task STARTED at {}", LocalDateTime.now());
        try {
            holidayService.checkAndImportYearlyHolidays();
            log.info("✅ Scheduled task COMPLETED at {}", LocalDateTime.now());
        } catch (Exception e) {
            log.error("❌ Scheduled task FAILED: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void testImport() {
        System.out.println("Testing Scheduler - Forcing Import");
        log.info("TESTING SCHEDULER - FORCING IMPORT");
        holidayService.checkAndImportYearlyHolidays();
    }
}
