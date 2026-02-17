package com.edt.repository;

import com.edt.entities.Matiere;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatiereRepository extends JpaRepository<Matiere, String> {
    Matiere findByCode(String code);
    
    // Recherche avec pagination - CORRIGÉE pour utiliser 'cycle' au lieu de 'niveau'
    @Query("SELECT m FROM Matiere m WHERE " +
           "LOWER(m.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.cycle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(m.niveauClasse) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Matiere> searchMatieres(@Param("search") String search, Pageable pageable);
}