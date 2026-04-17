package com.edt.repository;

import com.edt.entities.CreneauHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreneauHoraireRepository extends JpaRepository<CreneauHoraire, String> {
    
    List<CreneauHoraire> findByEmploiDuTempsId(String emploiDuTempsId);
    
    List<CreneauHoraire> findByEnseignantId(String enseignantId);
    
    List<CreneauHoraire> findByClasseId(String classeId);
    
    // ✅ CORRECTION ICI - Syntaxe JPA correcte
    List<CreneauHoraire> findByEnseignantIdAndEmploiDuTemps_AnneeScolaire(
        String enseignantId, String anneeScolaire);
    
    @Query("SELECT c FROM CreneauHoraire c WHERE " +
           "c.enseignant.id = :enseignantId AND " +
           "c.emploiDuTemps.anneeScolaire = :anneeScolaire AND " +
           "c.estLibre = false")
    List<CreneauHoraire> findCoursByEnseignantAndAnnee(
        @Param("enseignantId") String enseignantId, 
        @Param("anneeScolaire") String anneeScolaire);
}