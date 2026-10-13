package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.PrescriptionRequest;
import com.mediconnect.backend.dto.PrescriptionResponse;
import com.mediconnect.backend.exception.ResourceNotFoundException;
import com.mediconnect.backend.model.Consultation;
import com.mediconnect.backend.model.Prescription;
import com.mediconnect.backend.repository.PatientRepository;
import com.mediconnect.backend.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationService consultationService;
    private final PatientRepository patientRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               ConsultationService consultationService,
                               PatientRepository patientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.consultationService = consultationService;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PrescriptionResponse issuePrescription(PrescriptionRequest request) {
        Consultation consultation = consultationService.getConsultationEntity(request.getConsultationId());

        Prescription prescription = Prescription.builder()
                .consultation(consultation)
                .details(request.getDetails())
                .build();

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Prescription issued successfully with ID: {}", saved.getPrescriptionId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsByPatientId(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }

        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PrescriptionResponse mapToResponse(Prescription prescription) {
        Consultation consultation = prescription.getConsultation();
        return PrescriptionResponse.builder()
                .prescriptionId(prescription.getPrescriptionId())
                .consultationId(consultation.getConsultationId())
                .appointmentId(consultation.getAppointment().getAppointmentId())
                .patientId(consultation.getAppointment().getPatient().getPatientId())
                .patientName(consultation.getAppointment().getPatient().getName())
                .doctorId(consultation.getAppointment().getDoctor().getDoctorId())
                .doctorName(consultation.getAppointment().getDoctor().getName())
                .details(prescription.getDetails())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}

