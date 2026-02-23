// C:\projets\java\edt-generator\backend\src\main\java\com\edt\repository\UtilisateurRepository.java
package com.edt.repository;

import com.edt.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
}