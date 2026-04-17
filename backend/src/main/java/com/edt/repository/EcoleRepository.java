package com.edt.repository;

import com.edt.entities.Ecole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EcoleRepository extends JpaRepository<Ecole, String> {
    Optional<Ecole> findFirstByOrderByCreatedAtAsc(); // Pour récupérer la première école créée
}
