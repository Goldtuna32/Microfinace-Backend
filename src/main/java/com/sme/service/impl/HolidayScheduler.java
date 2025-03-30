package com.sme.service.impl;

import com.sme.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class HolidayScheduler {

    private final HolidayService holidayService;

    // Runs at 00:05 on January 1st every year
    @
            Scheduled(cron = "0 5 0 1 1 *")
    public void importHolidaysForNewYear() {
        holidayService.checkAndImportYearlyHolidays();
    }

    // Optional: Also run a check daily in case the service was down on Jan 1
    @Scheduled(cron = "0 0 0 * * *")
    public void dailyHolidayCheck() {
        LocalDate today = LocalDate.now();
        if (today.getMonthValue() == 1 && today.getDayOfMonth() <= 7) {
            // Only check in first week of January
            holidayService.checkAndImportYearlyHolidays();
        }
    }
}