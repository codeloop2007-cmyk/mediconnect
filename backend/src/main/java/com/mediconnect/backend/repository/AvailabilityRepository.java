package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByDoctorDoctorIdAndDate(Long doctorId, LocalDate date);
    List<Availability> findByDoctorDoctorId(Long doctorId);
    boolean existsByDoctorDoctorIdAndDateAndStartTimeAndEndTime(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime);
}

