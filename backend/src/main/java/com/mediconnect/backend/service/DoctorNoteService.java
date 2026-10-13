package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.DoctorNoteRequest;
import com.mediconnect.backend.dto.DoctorNoteResponse;
import com.mediconnect.backend.exception.ResourceNotFoundException;
import com.mediconnect.backend.model.Doctor;
import com.mediconnect.backend.model.DoctorNote;
import com.mediconnect.backend.model.Patient;
import com.mediconnect.backend.repository.DoctorNoteRepository;
import com.mediconnect.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorNoteService {

    private final DoctorNoteRepository doctorNoteRepository;
    private final DoctorService doctorService;
    private final PatientRepository patientRepository;

    public DoctorNoteService(DoctorNoteRepository doctorNoteRepository,
                             DoctorService doctorService,
                             PatientRepository patientRepository) {
        this.doctorNoteRepository = doctorNoteRepository;
        this.doctorService = doctorService;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public DoctorNoteResponse createDoctorNote(DoctorNoteRequest request) {
        Doctor doctor = doctorService.getDoctorEntity(request.getDoctorId());
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        DoctorNote doctorNote = DoctorNote.builder()
                .doctor(doctor)
                .patient(patient)
                .note(request.getNote())
                .build();

        DoctorNote saved = doctorNoteRepository.save(doctorNote);
        log.info("Clinical note added with ID: {}", saved.getNoteId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorNoteResponse> getDoctorNotesByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }

        return doctorNoteRepository.findByPatientPatientId(patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorNoteResponse> getDoctorNotesByDoctorAndPatient(Long doctorId, Long patientId) {
        doctorService.getDoctorEntity(doctorId);
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with ID: " + patientId);
        }

        return doctorNoteRepository.findByDoctorDoctorIdAndPatientPatientId(doctorId, patientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DoctorNoteResponse mapToResponse(DoctorNote note) {
        return DoctorNoteResponse.builder()
                .noteId(note.getNoteId())
                .doctorId(note.getDoctor().getDoctorId())
                .doctorName(note.getDoctor().getName())
                .patientId(note.getPatient().getPatientId())
                .patientName(note.getPatient().getName())
                .note(note.getNote())
                .createdAt(note.getCreatedAt())
                .build();
    }
}

