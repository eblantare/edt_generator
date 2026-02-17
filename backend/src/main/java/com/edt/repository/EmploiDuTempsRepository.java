package com.edt.repository;

import com.edt.entities.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, String> {
    List<EmploiDuTemps> findByClasseId(String classeId);
    List<EmploiDuTemps> findByEstGlobalTrue();
    List<EmploiDuTemps> findByAnneeScolaire(String anneeScolaire);
    List<EmploiDuTemps> findByStatut(String statut);
    
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.classe.id = :classeId AND e.anneeScolaire = :anneeScolaire")
    Optional<EmploiDuTemps> findByClasseAndAnneeScolaire(@Param("classeId") String classeId, 
                                                         @Param("anneeScolaire") String anneeScolaire);

    List<EmploiDuTemps> findByClasseIdAndAnneeScolaire(String classeId, String anneeScolaire);
}