package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientPatientId(Long patientId);
    List<Appointment> findByDoctorDoctorId(Long doctorId);
    boolean existsByDoctorDoctorIdAndDateAndTimeAndStatusNot(Long doctorId, LocalDate date, LocalTime time, AppointmentStatus status);
}

