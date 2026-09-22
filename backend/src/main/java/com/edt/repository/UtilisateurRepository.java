// C:\projets\java\edt-generator\backend\src\main\java\com\edt\repository\UtilisateurRepository.java
package com.edt.repository;

import com.edt.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    
    // Recherche exacte (existante)
    Optional<Utilisateur> findByEmail(String email);
    
    // NOUVEAU : Recherche insensible à la casse
    Optional<Utilisateur> findByEmailIgnoreCase(String email);
    
    // Vérification exacte (existante)
    boolean existsByEmail(String email);
    
    // NOUVEAU : Vérification insensible à la casse
    boolean existsByEmailIgnoreCase(String email);
}