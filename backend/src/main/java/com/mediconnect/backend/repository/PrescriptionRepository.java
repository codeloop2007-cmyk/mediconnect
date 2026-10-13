package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @Query("SELECT p FROM Prescription p " +
           "JOIN p.consultation c " +
           "JOIN c.appointment a " +
           "WHERE a.patient.patientId = :patientId")
    List<Prescription> findByPatientId(@Param("patientId") Long patientId);
}

