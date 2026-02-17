// C:\projets\java\edt-generator\backend\src\main\java\com\edt\repository\ClasseRepository.java
package com.edt.repository;

import com.edt.entities.Classe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, String> {
    Classe findByNom(String nom);
    
    // Recherche avec pagination
    @Query("SELECT c FROM Classe c WHERE " +
           "LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.niveau) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.filiere) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Classe> searchClasses(@Param("search") String search, Pageable pageable);
    
    // Trouver toutes les classes avec pagination et tri
    Page<Classe> findAll(Pageable pageable);
}