// C:\projets\java\edt-generator\backend\src\main\java\com\edt\repository\CodeConnexionRepository.java
package com.edt.repository;

import com.edt.entities.CodeConnexion;
import com.edt.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodeConnexionRepository extends JpaRepository<CodeConnexion, String> {
    Optional<CodeConnexion> findByUtilisateurAndCodeAndEstUtiliseFalse(Utilisateur utilisateur, String code);
    List<CodeConnexion> findByUtilisateurAndEstUtiliseFalse(Utilisateur utilisateur);
    void deleteByUtilisateurAndEstUtiliseFalse(Utilisateur utilisateur);

    // Dans CodeConnexionRepository.java, ajoutez :
    @Modifying
    @Query("DELETE FROM CodeConnexion c WHERE c.utilisateur.id = :utilisateurId AND c.estUtilise = false")
    void deleteByUtilisateurId(@Param("utilisateurId") String utilisateurId);
}