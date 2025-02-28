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

    // ✅ Fetch Myanmar public holidays and store them
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

    // ✅ Get holidays by branch
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<Holiday>> getHolidaysByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(holidayService.getHolidaysByBranch(branchId));
    }
}
