package com.edt.repository;

import com.edt.entities.Enseignant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnseignantRepository extends JpaRepository<Enseignant, String> {
    
    @Query("SELECT e FROM Enseignant e WHERE " +
           "LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.matricule) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Enseignant> findBySearch(@Param("search") String search, Pageable pageable);
    
    Enseignant findByMatricule(String matricule);
}