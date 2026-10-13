package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.AvailabilityRequest;
import com.mediconnect.backend.dto.AvailabilityResponse;
import com.mediconnect.backend.exception.BadRequestException;
import com.mediconnect.backend.exception.DuplicateResourceException;
import com.mediconnect.backend.model.Availability;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.repository.AvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = Doctor.builder()
                .doctorId(1L)
                .name("Dr. Strange")
                .specialization("Neurology")
                .build();
    }

    @Test
    @DisplayName("Successfully create availability slot")
    void testCreateAvailability_Success() {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .doctorId(1L)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        when(doctorService.getDoctorEntity(1L)).thenReturn(doctor);
        when(availabilityRepository.existsByDoctorDoctorIdAndDateAndStartTimeAndEndTime(
                eq(1L), eq(request.getDate()), eq(request.getStartTime()), eq(request.getEndTime())
        )).thenReturn(false);

        Availability saved = Availability.builder()
                .availabilityId(100L)
                .doctor(doctor)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();
        when(availabilityRepository.save(any(Availability.class))).thenReturn(saved);

        AvailabilityResponse response = availabilityService.createAvailability(request);

        assertNotNull(response);
        assertEquals(100L, response.getAvailabilityId());
        assertEquals(1L, response.getDoctorId());
        assertEquals("Dr. Strange", response.getDoctorName());
        assertEquals(LocalTime.of(9, 0), response.getStartTime());
    }

    @Test
    @DisplayName("Throw BadRequestException when end time is before or equal to start time")
    void testCreateAvailability_InvalidTimes() {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .doctorId(1L)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(10, 0))
                .build();

        assertThrows(BadRequestException.class, () -> availabilityService.createAvailability(request));
        verify(availabilityRepository, never()).save(any(Availability.class));
    }

    @Test
    @DisplayName("Throw DuplicateResourceException when slot already exists")
    void testCreateAvailability_DuplicateSlot() {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .doctorId(1L)
                .date(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        when(doctorService.getDoctorEntity(1L)).thenReturn(doctor);
        when(availabilityRepository.existsByDoctorDoctorIdAndDateAndStartTimeAndEndTime(
                eq(1L), eq(request.getDate()), eq(request.getStartTime()), eq(request.getEndTime())
        )).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> availabilityService.createAvailability(request));
        verify(availabilityRepository, never()).save(any(Availability.class));
    }

    @Test
    @DisplayName("Get availabilities by doctor ID")
    void testGetAvailabilitiesByDoctor() {
        when(doctorService.getDoctorEntity(1L)).thenReturn(doctor);
        Availability slot = Availability.builder()
                .availabilityId(101L)
                .doctor(doctor)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();
        when(availabilityRepository.findByDoctorDoctorId(1L)).thenReturn(List.of(slot));

        List<AvailabilityResponse> list = availabilityService.getAvailabilitiesByDoctor(1L);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(101L, list.get(0).getAvailabilityId());
    }
}

