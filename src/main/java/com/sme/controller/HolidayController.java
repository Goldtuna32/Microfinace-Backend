package com.sme.controller;

import com.sme.entity.Holiday;
import com.sme.service.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @PostMapping("/import")
    public ResponseEntity<String> importMyanmarHolidays(@RequestParam int year) {
        try {
            holidayService.importMyanmarHolidays(year);
            return ResponseEntity.ok("Myanmar holidays imported successfully for year " + year);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/generate-weekends/{year}")
    public ResponseEntity<String> generateWeekends(@PathVariable int year) {
        holidayService.generateWeekendsForYear(year);
        return ResponseEntity.ok("Weekend holidays for year " + year + " added successfully!");
    }

    @GetMapping
    public ResponseEntity<List<Holiday>> getAllHolidays() {
        return ResponseEntity.ok(holidayService.getAllHolidays());
    }

    @GetMapping("/year/{year}")
    public ResponseEntity<List<Holiday>> getHolidaysByYear(@PathVariable int year) {
        return ResponseEntity.ok(holidayService.getHolidaysByYear(year));
    }

    @PostMapping("/import-current-year")
    public ResponseEntity<String> importCurrentYearHolidays() {
        holidayService.checkAndImportYearlyHolidays();
        return ResponseEntity.ok("Holiday import process triggered for current year");
    }
}
