package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.DoctorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {
    List<DoctorNote> findByDoctorDoctorIdAndPatientPatientId(Long doctorId, Long patientId);
    List<DoctorNote> findByPatientPatientId(Long patientId);
}

