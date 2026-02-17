package com.edt.repository;

import com.edt.entities.HoraireJournalier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoraireJournalierRepository extends JpaRepository<HoraireJournalier, String> {
    Optional<HoraireJournalier> findByJourSemaine(String jourSemaine);
    
    // CORRECTION : Utilisez findByEstJourCours au lieu de findByJourCours
    List<HoraireJournalier> findByEstJourCours(Boolean estJourCours);
    
    List<HoraireJournalier> findByEstJourCoursTrue();
    List<HoraireJournalier> findByEstJourCoursFalse();
}