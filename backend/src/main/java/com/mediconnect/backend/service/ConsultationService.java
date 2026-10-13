package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.ConsultationRequest;
import com.mediconnect.backend.dto.ConsultationResponse;
import com.mediconnect.backend.exception.DuplicateResourceException;
import com.mediconnect.backend.exception.ResourceNotFoundException;
import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.model.AppointmentStatus;
import com.mediconnect.backend.model.Consultation;
import com.mediconnect.backend.repository.AppointmentRepository;
import com.mediconnect.backend.repository.ConsultationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    public ConsultationService(ConsultationRepository consultationRepository,
                               AppointmentRepository appointmentRepository,
                               AppointmentService appointmentService) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
    }

    @Transactional
    public ConsultationResponse createConsultation(ConsultationRequest request) {
        Appointment appointment = appointmentService.getAppointmentEntity(request.getAppointmentId());

        if (consultationRepository.findByAppointmentAppointmentId(appointment.getAppointmentId()).isPresent()) {
            throw new DuplicateResourceException("Consultation already recorded for appointment ID: "
                    + request.getAppointmentId());
        }

        LocalDateTime consultationDate = request.getConsultationDate() != null
                ? request.getConsultationDate()
                : LocalDateTime.now();

        Consultation consultation = Consultation.builder()
                .appointment(appointment)
                .consultationDate(consultationDate)
                .build();

        Consultation saved = consultationRepository.save(consultation);

        // Update appointment status to COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        log.info("Consultation recorded successfully with ID: {}", saved.getConsultationId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationByAppointmentId(Long appointmentId) {
        Consultation consultation = consultationRepository.findByAppointmentAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found for appointment ID: " + appointmentId));
        return mapToResponse(consultation);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationById(Long consultationId) {
        Consultation consultation = getConsultationEntity(consultationId);
        return mapToResponse(consultation);
    }

    public Consultation getConsultationEntity(Long consultationId) {
        return consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with ID: " + consultationId));
    }

    private ConsultationResponse mapToResponse(Consultation consultation) {
        return ConsultationResponse.builder()
                .consultationId(consultation.getConsultationId())
                .appointmentId(consultation.getAppointment().getAppointmentId())
                .patientId(consultation.getAppointment().getPatient().getPatientId())
                .patientName(consultation.getAppointment().getPatient().getName())
                .doctorId(consultation.getAppointment().getDoctor().getDoctorId())
                .doctorName(consultation.getAppointment().getDoctor().getName())
                .consultationDate(consultation.getConsultationDate())
                .build();
    }
}

