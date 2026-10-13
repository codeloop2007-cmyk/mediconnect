package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.DoctorRequest;
import com.mediconnect.backend.dto.DoctorResponse;
import com.mediconnect.backend.exception.ResourceNotFoundException;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.repository.DoctorRepository;
import com.mediconnect.backend.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.mediconnect.backend.client.AiServiceClient;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ReviewRepository reviewRepository;
    private final AiServiceClient aiServiceClient;

    public DoctorService(DoctorRepository doctorRepository, ReviewRepository reviewRepository, AiServiceClient aiServiceClient) {
        this.doctorRepository = doctorRepository;
        this.reviewRepository = reviewRepository;
        this.aiServiceClient = aiServiceClient;
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> searchDoctors(String specialization, String language, String location, BigDecimal maxFee) {
        boolean hasSpec = specialization != null && !specialization.isBlank();
        boolean hasLang = language != null && !language.isBlank();
        boolean hasLoc = location != null && !location.isBlank();
        boolean hasFee = maxFee != null;

        List<Doctor> doctors;
        if (!hasSpec && !hasLang && !hasLoc && !hasFee) {
            doctors = doctorRepository.findAll();
        } else {
            doctors = doctorRepository.searchDoctors(
                    hasSpec ? specialization.trim().toLowerCase() : null,
                    hasLang ? language.trim().toLowerCase() : null,
                    hasLoc ? location.trim().toLowerCase() : null,
                    maxFee
            );
        }

        return doctors.stream()
                .map(this::mapToDoctorResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
        return mapToDoctorResponse(doctor);
    }

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        Doctor doctor = Doctor.builder()
                .name(request.getName())
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .language(request.getLanguage())
                .location(request.getLocation())
                .consultationFee(request.getConsultationFee())
                .build();

        Doctor savedDoctor = doctorRepository.save(doctor);

        // Generate vector embedding via Python AI microservice and update PostgreSQL pgvector
        String profileText = savedDoctor.getName() + " " +
                savedDoctor.getSpecialization() + " " +
                (savedDoctor.getQualification() != null ? savedDoctor.getQualification() : "") + " " +
                (savedDoctor.getLocation() != null ? savedDoctor.getLocation() : "");
        String vectorString = aiServiceClient.getEmbeddingVectorString(profileText);
        if (vectorString != null) {
            doctorRepository.updateDoctorEmbedding(savedDoctor.getDoctorId(), vectorString);
        }

        return mapToDoctorResponse(savedDoctor);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> semanticSearchDoctors(String query, int limit) {
        if (query == null || query.isBlank()) {
            return searchDoctors(null, null, null, null);
        }

        String vectorString = aiServiceClient.getEmbeddingVectorString(query);
        if (vectorString == null) {
            // Fallback to keyword search if AI microservice is not reachable
            return searchDoctors(query, null, null, null);
        }

        int safeLimit = (limit > 0 && limit <= 50) ? limit : 5;
        List<Doctor> nearestDoctors = doctorRepository.findNearestDoctorsByVector(vectorString, safeLimit);
        return nearestDoctors.stream()
                .map(this::mapToDoctorResponse)
                .collect(Collectors.toList());
    }

    public Doctor getDoctorEntity(Long doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));
    }

    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        Double avgRating = reviewRepository.getAverageRatingForDoctor(doctor.getDoctorId());
        int totalReviews = reviewRepository.findByDoctorDoctorId(doctor.getDoctorId()).size();

        double roundedRating = avgRating != null
                ? BigDecimal.valueOf(avgRating).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return DoctorResponse.builder()
                .doctorId(doctor.getDoctorId())
                .name(doctor.getName())
                .specialization(doctor.getSpecialization())
                .qualification(doctor.getQualification())
                .language(doctor.getLanguage())
                .location(doctor.getLocation())
                .consultationFee(doctor.getConsultationFee())
                .averageRating(roundedRating)
                .totalReviews(totalReviews)
                .build();
    }
}

