package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.AppointmentRequest;
import com.mediconnect.backend.dto.AppointmentResponse;
import com.mediconnect.backend.dto.AppointmentStatusUpdateRequest;
import com.mediconnect.backend.exception.AppointmentConflictException;
import com.mediconnect.backend.exception.BadRequestException;
import com.mediconnect.backend.exception.ResourceNotFoundException;
import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.model.AppointmentStatus;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.model.Patient;
import com.mediconnect.backend.repository.AppointmentRepository;
import com.mediconnect.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              DoctorService doctorService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
    }

    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        Doctor doctor = doctorService.getDoctorEntity(request.getDoctorId());

        // Double-booking check: ensure doctor is not already booked at date and time unless CANCELLED
        boolean conflict = appointmentRepository.existsByDoctorDoctorIdAndDateAndTimeAndStatusNot(
                doctor.getDoctorId(),
                request.getDate(),
                request.getTime(),
                AppointmentStatus.CANCELLED
        );

        if (conflict) {
            throw new AppointmentConflictException(String.format(
                    "Doctor %s is already booked on %s at %s. Please select another slot.",
                    doctor.getName(), request.getDate(), request.getTime()
            ));
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .date(request.getDate())
                .time(request.getTime())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment booked successfully with ID: {}", saved.getAppointmentId());
        return mapToResponse(saved);
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long appointmentId, AppointmentStatusUpdateRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        if (request.getStatus() == AppointmentStatus.RESCHEDULED) {
            LocalDate newDate = request.getNewDate() != null ? request.getNewDate() : appointment.getDate();
            LocalTime newTime = request.getNewTime() != null ? request.getNewTime() : appointment.getTime();

            if (request.getNewDate() == null && request.getNewTime() == null) {
                throw new BadRequestException("New date or new time must be provided to reschedule an appointment");
            }

            // Conflict check for new slot
            boolean conflict = appointmentRepository.existsByDoctorDoctorIdAndDateAndTimeAndStatusNot(
                    appointment.getDoctor().getDoctorId(),
                    newDate,
                    newTime,
                    AppointmentStatus.CANCELLED
            );

            if (conflict) {
                throw new AppointmentConflictException(String.format(
                        "Cannot reschedule: Doctor %s is already booked on %s at %s.",
                        appointment.getDoctor().getName(), newDate, newTime
                ));
            }

            appointment.setDate(newDate);
            appointment.setTime(newTime);
            appointment.setStatus(AppointmentStatus.RESCHEDULED);
        } else {
            appointment.setStatus(request.getStatus());
        }

        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        // verify patient exists
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }

        return appointmentRepository.findByPatientPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        // verify doctor exists
        doctorService.getDoctorEntity(doctorId);

        return appointmentRepository.findByDoctorDoctorId(doctorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long appointmentId) {
        Appointment appointment = getAppointmentEntity(appointmentId);
        return mapToResponse(appointment);
    }

    public Appointment getAppointmentEntity(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .appointmentId(appointment.getAppointmentId())
                .patientId(appointment.getPatient().getPatientId())
                .patientName(appointment.getPatient().getName())
                .doctorId(appointment.getDoctor().getDoctorId())
                .doctorName(appointment.getDoctor().getName())
                .doctorSpecialization(appointment.getDoctor().getSpecialization())
                .date(appointment.getDate())
                .time(appointment.getTime())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}

