package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query("SELECT d FROM Doctor d WHERE " +
           "(:specialization IS NULL OR LOWER(d.specialization) LIKE CONCAT('%', CAST(:specialization AS string), '%')) AND " +
           "(:language IS NULL OR LOWER(d.language) LIKE CONCAT('%', CAST(:language AS string), '%')) AND " +
           "(:location IS NULL OR LOWER(d.location) LIKE CONCAT('%', CAST(:location AS string), '%')) AND " +
           "(:maxFee IS NULL OR d.consultationFee <= :maxFee)")
    List<Doctor> searchDoctors(@Param("specialization") String specialization,
                               @Param("language") String language,
                               @Param("location") String location,
                               @Param("maxFee") BigDecimal maxFee);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "UPDATE doctors SET embedding = CAST(:vector AS vector) WHERE doctor_id = :doctorId", nativeQuery = true)
    void updateDoctorEmbedding(@Param("doctorId") Long doctorId, @Param("vector") String vector);

    @Query(value = "SELECT * FROM doctors WHERE embedding IS NOT NULL ORDER BY embedding <=> CAST(:vector AS vector) LIMIT :limit", nativeQuery = true)
    List<Doctor> findNearestDoctorsByVector(@Param("vector") String vector, @Param("limit") int limit);
}

