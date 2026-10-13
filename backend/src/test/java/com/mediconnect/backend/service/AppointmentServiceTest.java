package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.AppointmentRequest;
import com.mediconnect.backend.dto.AppointmentResponse;
import com.mediconnect.backend.dto.AppointmentStatusUpdateRequest;
import com.mediconnect.backend.exception.AppointmentConflictException;
import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.model.AppointmentStatus;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.model.Patient;
import com.mediconnect.backend.repository.AppointmentRepository;
import com.mediconnect.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .patientId(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();

        doctor = Doctor.builder()
                .doctorId(2L)
                .name("Dr. Smith")
                .specialization("Cardiology")
                .qualification("MD, FACC")
                .language("English")
                .location("Boston Medical")
                .consultationFee(BigDecimal.valueOf(150.00))
                .build();
    }

    @Test
    @DisplayName("Successfully book appointment when slot is available")
    void testBookAppointment_Success() {
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(2L)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(10, 0))
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorService.getDoctorEntity(2L)).thenReturn(doctor);
        when(appointmentRepository.existsByDoctorDoctorIdAndDateAndTimeAndStatusNot(
                eq(2L), eq(request.getDate()), eq(request.getTime()), eq(AppointmentStatus.CANCELLED)
        )).thenReturn(false);

        Appointment savedAppointment = Appointment.builder()
                .appointmentId(10L)
                .patient(patient)
                .doctor(doctor)
                .date(request.getDate())
                .time(request.getTime())
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        AppointmentResponse response = appointmentService.bookAppointment(request);

        assertNotNull(response);
        assertEquals(10L, response.getAppointmentId());
        assertEquals(1L, response.getPatientId());
        assertEquals(2L, response.getDoctorId());
        assertEquals(AppointmentStatus.SCHEDULED, response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Throw AppointmentConflictException when double booking occurs")
    void testBookAppointment_DoubleBookingConflict() {
        AppointmentRequest request = AppointmentRequest.builder()
                .patientId(1L)
                .doctorId(2L)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(10, 0))
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorService.getDoctorEntity(2L)).thenReturn(doctor);
        when(appointmentRepository.existsByDoctorDoctorIdAndDateAndTimeAndStatusNot(
                eq(2L), eq(request.getDate()), eq(request.getTime()), eq(AppointmentStatus.CANCELLED)
        )).thenReturn(true);

        assertThrows(AppointmentConflictException.class, () -> appointmentService.bookAppointment(request));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Successfully cancel an existing appointment")
    void testUpdateStatus_Cancel() {
        Appointment appointment = Appointment.builder()
                .appointmentId(10L)
                .patient(patient)
                .doctor(doctor)
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(10, 0))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentStatusUpdateRequest updateRequest = AppointmentStatusUpdateRequest.builder()
                .status(AppointmentStatus.CANCELLED)
                .build();

        AppointmentResponse response = appointmentService.updateAppointmentStatus(10L, updateRequest);

        assertNotNull(response);
        assertEquals(AppointmentStatus.CANCELLED, response.getStatus());
    }
}

