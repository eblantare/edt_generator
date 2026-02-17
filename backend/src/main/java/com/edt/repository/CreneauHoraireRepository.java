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
    List<CreneauHoraire> findByJourSemaineAndHeureDebut(String jourSemaine, String heureDebut);
    
    @Query("SELECT c FROM CreneauHoraire c WHERE c.emploiDuTemps.id = :emploiId AND c.jourSemaine = :jour")
    List<CreneauHoraire> findByEmploiAndJour(@Param("emploiId") String emploiId, 
                                             @Param("jour") String jour);
    
    @Query("SELECT c FROM CreneauHoraire c WHERE c.emploiDuTemps.id = :emploiId AND c.enseignant.id = :enseignantId")
    List<CreneauHoraire> findByEmploiAndEnseignant(@Param("emploiId") String emploiId, 
                                                   @Param("enseignantId") String enseignantId);
    
    @Query("SELECT c FROM CreneauHoraire c WHERE c.emploiDuTemps.id = :emploiId AND c.jourSemaine = :jour AND c.heureDebut = :heure")
    List<CreneauHoraire> findByEmploiDuTempsIdAndJourSemaineAndHeureDebut(
            @Param("emploiId") String emploiId, 
            @Param("jour") String jour, 
            @Param("heure") String heure);
}