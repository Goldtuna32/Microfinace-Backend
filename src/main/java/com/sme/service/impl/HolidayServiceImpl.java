package com.sme.service.impl;

import com.sme.entity.Branch;
import com.sme.entity.Holiday;
import com.sme.repository.BranchRepository;
import com.sme.repository.HolidayRepository;
import com.sme.service.GoogleCalendarService;
import com.sme.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HolidayServiceImpl implements HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private GoogleCalendarService googleCalendarService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    @Transactional
    public void importMyanmarHolidays(int year) throws Exception {
        List<Map<String, String>> holidays = googleCalendarService.fetchMyanmarHolidays(year);

        for (Map<String, String> holidayData : holidays) {
            Date holidayDate = dateFormat.parse(holidayData.get("date"));
            String holidayName = holidayData.get("name");

            // Check if holiday already exists (no branch check needed now)
            boolean exists = holidayRepository.existsByHolidayDate(holidayDate);
            if (!exists) {
                Holiday holiday = new Holiday();
                holiday.setHolidayDate(holidayDate);
                holiday.setDescription(holidayName);
                holidayRepository.save(holiday);
                log.debug("Added holiday: {} on {}", holidayName, holidayDate);
            }
        }
    }

    @Transactional
    @Override
    public void generateWeekendsForYear(int year) {
        List<Holiday> holidays = new ArrayList<>();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        while (!startDate.isAfter(endDate)) {
            DayOfWeek dayOfWeek = startDate.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                Date weekendDate = java.sql.Date.valueOf(startDate);

                // Check if weekend already marked as holiday
                if (!holidayRepository.existsByHolidayDate(weekendDate)) {
                    Holiday holiday = new Holiday();
                    holiday.setHolidayDate(weekendDate);
                    holiday.setDescription(dayOfWeek.name() + " (Weekend)");
                    holidays.add(holiday);
                }
            }
            startDate = startDate.plusDays(1);
        }

        if (!holidays.isEmpty()) {
            holidayRepository.saveAll(holidays);
            log.info("Added {} weekend holidays for year {}", holidays.size(), year);
        }
    }

    @Override
    public List<Holiday> getAllHolidays() {
        return holidayRepository.findAll();
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        return holidayRepository.existsByHolidayDate(java.sql.Date.valueOf(date));
    }


    @Override
    @Transactional
    public void checkAndImportYearlyHolidays() {
        int currentYear = LocalDate.now().getYear();
        log.info("🔍 Checking holidays for year {}", currentYear);

        Date startDate = java.sql.Date.valueOf(LocalDate.of(currentYear, 1, 1));
        Date endDate = java.sql.Date.valueOf(LocalDate.of(currentYear, 12, 31));

        long existingCount = holidayRepository.countByHolidayDateBetween(startDate, endDate);
        log.info("📊 Found {} existing holidays for {}", existingCount, currentYear);

        if (existingCount == 0) {
            try {
                log.info("🔄 Importing holidays...");
                this.importMyanmarHolidays(currentYear);
                this.generateWeekendsForYear(currentYear);
                long newCount = holidayRepository.countByHolidayDateBetween(startDate, endDate);
                log.info("🎉 Successfully imported {} holidays", newCount);
            } catch (Exception e) {
                log.error("💥 Import failed: {}", e.getMessage());
            }
        }
    }

    @Override
    public List<Holiday> getHolidaysByYear(int year) {
        // Option 1: Using the repository method we defined
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return holidayRepository.findByHolidayDateBetween(
                java.sql.Date.valueOf(startDate),
                java.sql.Date.valueOf(endDate)
        );
    }
}
