// C:\projets\java\edt-generator\backend\src\main\java\com\edt\repository\EnseignementRepository.java
package com.edt.repository;

import com.edt.entities.Enseignement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EnseignementRepository extends JpaRepository<Enseignement, String> {
    List<Enseignement> findByEnseignantId(String enseignantId);
    List<Enseignement> findByClasseId(String classeId);
    List<Enseignement> findByMatiereId(String matiereId);
    List<Enseignement> findByEnseignantIdAndClasseId(String enseignantId, String classeId);
    List<Enseignement> findByEnseignantIdAndMatiereId(String enseignantId, String matiereId);
    List<Enseignement> findByClasseIdAndMatiereId(String classeId, String matiereId);
    List<Enseignement> findByEnseignantIdAndClasseIdAndMatiereId(String enseignantId, String classeId, String matiereId);
    // Ajoutez cette méthode dans EnseignementRepository.java
    @Query("SELECT e FROM Enseignement e WHERE " +
       "LOWER(e.enseignant.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(e.enseignant.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(e.matiere.code) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(e.matiere.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
       "LOWER(e.classe.nom) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Enseignement> searchEnseignements(@Param("search") String search, Pageable pageable);
}