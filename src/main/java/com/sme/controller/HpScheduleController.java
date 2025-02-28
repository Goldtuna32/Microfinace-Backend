package com.sme.controller;

import com.sme.dto.HpScheduleDTO;
import com.sme.service.HpScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hp-schedule")
public class HpScheduleController {

    @Autowired
    private HpScheduleService hpScheduleService;

    @PostMapping("/generate/{hpRegistrationId}")
    public ResponseEntity<List<HpScheduleDTO>> generateSchedule(@PathVariable Long hpRegistrationId) {
        List<HpScheduleDTO> schedules = hpScheduleService.generateHpRepaymentSchedule(hpRegistrationId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/list/{hpRegistrationId}")
    public ResponseEntity<List<HpScheduleDTO>> getSchedules(@PathVariable Long hpRegistrationId) {
        List<HpScheduleDTO> schedules = hpScheduleService.getHpSchedulesByHpRegistrationId(hpRegistrationId);
        return ResponseEntity.ok(schedules);
    }
}
