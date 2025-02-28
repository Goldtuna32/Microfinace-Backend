package com.sme.service;

import com.sme.entity.Branch;
import com.sme.entity.Holiday;
import com.sme.repository.BranchRepository;
import com.sme.repository.HolidayRepository;
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

public interface HolidayService {

     void importMyanmarHolidays(int year) throws Exception;

     void generateWeekendsForYear(int year);

    List<Holiday> getHolidaysByBranch(Long branchId);
}
