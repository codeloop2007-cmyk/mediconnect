package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.ConsultationRequest;
import com.mediconnect.backend.dto.ConsultationResponse;
import com.mediconnect.backend.dto.PrescriptionRequest;
import com.mediconnect.backend.dto.PrescriptionResponse;
import com.mediconnect.backend.exception.DuplicateResourceException;
import com.mediconnect.backend.model.*;
import com.mediconnect.backend.repository.AppointmentRepository;
import com.mediconnect.backend.repository.ConsultationRepository;
import com.mediconnect.backend.repository.PatientRepository;
import com.mediconnect.backend.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationAndPrescriptionServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private ConsultationService consultationService;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    private PrescriptionService prescriptionService;

    private Appointment appointment;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        prescriptionService = new PrescriptionService(prescriptionRepository, consultationService, patientRepository);

        patient = Patient.builder()
                .patientId(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .build();

        doctor = Doctor.builder()
                .doctorId(2L)
                .name("Dr. Watson")
                .specialization("General")
                .build();

        appointment = Appointment.builder()
                .appointmentId(10L)
                .patient(patient)
                .doctor(doctor)
                .date(LocalDate.now())
                .time(LocalTime.of(15, 0))
                .status(AppointmentStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("Successfully create consultation and mark appointment COMPLETED")
    void testCreateConsultation_Success() {
        ConsultationRequest request = ConsultationRequest.builder()
                .appointmentId(10L)
                .consultationDate(LocalDateTime.now())
                .build();

        when(appointmentService.getAppointmentEntity(10L)).thenReturn(appointment);
        when(consultationRepository.findByAppointmentAppointmentId(10L)).thenReturn(Optional.empty());

        Consultation saved = Consultation.builder()
                .consultationId(50L)
                .appointment(appointment)
                .consultationDate(request.getConsultationDate())
                .build();
        when(consultationRepository.save(any(Consultation.class))).thenReturn(saved);

        ConsultationResponse response = consultationService.createConsultation(request);

        assertNotNull(response);
        assertEquals(50L, response.getConsultationId());
        assertEquals(10L, response.getAppointmentId());
        assertEquals("Jane Doe", response.getPatientName());
        assertEquals("Dr. Watson", response.getDoctorName());
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    @DisplayName("Throw DuplicateResourceException when consultation already recorded")
    void testCreateConsultation_AlreadyRecorded() {
        ConsultationRequest request = ConsultationRequest.builder()
                .appointmentId(10L)
                .build();

        when(appointmentService.getAppointmentEntity(10L)).thenReturn(appointment);
        when(consultationRepository.findByAppointmentAppointmentId(10L))
                .thenReturn(Optional.of(new Consultation()));

        assertThrows(DuplicateResourceException.class, () -> consultationService.createConsultation(request));
        verify(consultationRepository, never()).save(any(Consultation.class));
    }

    @Test
    @DisplayName("Successfully issue prescription for consultation")
    void testIssuePrescription_Success() {
        Consultation consultation = Consultation.builder()
                .consultationId(50L)
                .appointment(appointment)
                .consultationDate(LocalDateTime.now())
                .build();

        when(consultationRepository.findById(50L)).thenReturn(Optional.of(consultation));

        PrescriptionRequest request = PrescriptionRequest.builder()
                .consultationId(50L)
                .details("Paracetamol 500mg, 1 tablet twice a day")
                .build();

        Prescription saved = Prescription.builder()
                .prescriptionId(80L)
                .consultation(consultation)
                .details(request.getDetails())
                .createdAt(LocalDateTime.now())
                .build();
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(saved);

        PrescriptionResponse response = prescriptionService.issuePrescription(request);

        assertNotNull(response);
        assertEquals(80L, response.getPrescriptionId());
        assertEquals(50L, response.getConsultationId());
        assertEquals("Paracetamol 500mg, 1 tablet twice a day", response.getDetails());
    }

    @Test
    @DisplayName("Get prescriptions by patient ID")
    void testGetPrescriptionsByPatientId() {
        Consultation consultation = Consultation.builder()
                .consultationId(50L)
                .appointment(appointment)
                .consultationDate(LocalDateTime.now())
                .build();

        Prescription p = Prescription.builder()
                .prescriptionId(80L)
                .consultation(consultation)
                .details("Amoxicillin 250mg")
                .createdAt(LocalDateTime.now())
                .build();

        when(patientRepository.existsById(1L)).thenReturn(true);
        when(prescriptionRepository.findByPatientId(1L)).thenReturn(List.of(p));

        List<PrescriptionResponse> list = prescriptionService.getPrescriptionsByPatientId(1L);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(80L, list.get(0).getPrescriptionId());
        assertEquals("Amoxicillin 250mg", list.get(0).getDetails());
    }
}

