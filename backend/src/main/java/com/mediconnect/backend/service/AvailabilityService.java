package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.AvailabilityRequest;
import com.mediconnect.backend.dto.AvailabilityResponse;
import com.mediconnect.backend.exception.BadRequestException;
import com.mediconnect.backend.exception.DuplicateResourceException;
import com.mediconnect.backend.model.Availability;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final DoctorService doctorService;

    public AvailabilityService(AvailabilityRepository availabilityRepository, DoctorService doctorService) {
        this.availabilityRepository = availabilityRepository;
        this.doctorService = doctorService;
    }

    @Transactional
    public AvailabilityResponse createAvailability(AvailabilityRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("End time must be strictly after start time");
        }

        Doctor doctor = doctorService.getDoctorEntity(request.getDoctorId());

        boolean exists = availabilityRepository.existsByDoctorDoctorIdAndDateAndStartTimeAndEndTime(
                doctor.getDoctorId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime()
        );
        if (exists) {
            throw new DuplicateResourceException("Availability slot already exists for doctor on date "
                    + request.getDate() + " between " + request.getStartTime() + " and " + request.getEndTime());
        }

        Availability availability = Availability.builder()
                .doctor(doctor)
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        Availability saved = availabilityRepository.save(availability);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailabilitiesByDoctor(Long doctorId) {
        // verify doctor exists
        doctorService.getDoctorEntity(doctorId);

        return availabilityRepository.findByDoctorDoctorId(doctorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailabilitiesByDoctorAndDate(Long doctorId, LocalDate date) {
        doctorService.getDoctorEntity(doctorId);

        return availabilityRepository.findByDoctorDoctorIdAndDate(doctorId, date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AvailabilityResponse mapToResponse(Availability availability) {
        return AvailabilityResponse.builder()
                .availabilityId(availability.getAvailabilityId())
                .doctorId(availability.getDoctor().getDoctorId())
                .doctorName(availability.getDoctor().getName())
                .date(availability.getDate())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .build();
    }
}

