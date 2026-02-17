package com.edt.controllers;

import com.edt.dtos.EnseignantDTO;
import com.edt.services.EnseignantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enseignants")
@CrossOrigin(origins = "http://localhost:4200")
public class EnseignantController {
    
    @Autowired
    private EnseignantService enseignantService;
    
    @GetMapping
    public ResponseEntity<Page<EnseignantDTO>> getAllEnseignants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "nom") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        System.out.println("====================");
        System.out.println("📥 API /api/enseignants appelée");
        System.out.println("📋 Paramètres:");
        System.out.println("  - page: " + page);
        System.out.println("  - size: " + size);
        System.out.println("  - search: '" + search + "'");
        System.out.println("  - sortBy: '" + sortBy + "'");
        System.out.println("  - sortDirection: '" + sortDirection + "'");
        
        // VALIDATION du paramètre sortDirection
        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            System.out.println("⚠️ Direction de tri invalide, utilisation de 'asc' par défaut");
            sortDirection = "asc";
        }
        
        // VALIDATION du paramètre sortBy
        String[] colonnesValides = {"nom", "prenom", "matricule", "email", "heuresMaxHebdo"};
        boolean colonneValide = false;
        for (String colonne : colonnesValides) {
            if (colonne.equalsIgnoreCase(sortBy)) {
                colonneValide = true;
                break;
            }
        }
        if (!colonneValide) {
            System.out.println("⚠️ Colonne de tri invalide '" + sortBy + "', utilisation de 'nom' par défaut");
            sortBy = "nom";
        }
        
        Page<EnseignantDTO> enseignants = enseignantService.getAllEnseignants(
            page, size, search, sortBy, sortDirection);
        
        System.out.println("📤 Réponse envoyée:");
        System.out.println("  - Nombre d'éléments: " + enseignants.getContent().size());
        System.out.println("  - Page actuelle: " + (enseignants.getNumber() + 1) + "/" + enseignants.getTotalPages());
        System.out.println("  - Total éléments: " + enseignants.getTotalElements());
        System.out.println("  - Tri appliqué: " + sortBy + " " + sortDirection);
        System.out.println("====================");
        
        return ResponseEntity.ok(enseignants);
    }
    
    @PostMapping
    public ResponseEntity<EnseignantDTO> createEnseignant(@RequestBody EnseignantDTO enseignantDTO) {
        System.out.println("📝 Création d'un nouvel enseignant");
        System.out.println("  Nom: " + enseignantDTO.getNom() + " " + enseignantDTO.getPrenom());
        System.out.println("  Matricule: " + enseignantDTO.getMatricule());
        
        EnseignantDTO created = enseignantService.createEnseignant(enseignantDTO);
        
        // ⭐⭐ AJOUT CRITIQUE : LOG POUR VÉRIFICATION ⭐⭐
        System.out.println("✅ Enseignant créé:");
        System.out.println("  - Matière dominante: " + (created.getMatiereDominante() != null ? created.getMatiereDominante().getNom() : "null"));
        System.out.println("  - Matière secondaire: " + (created.getMatiereSecondaire() != null ? created.getMatiereSecondaire().getNom() : "null"));
        
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EnseignantDTO> getEnseignant(@PathVariable String id) {
        System.out.println("🔍 Détails enseignant ID: " + id);
        
        EnseignantDTO enseignant = enseignantService.getEnseignantById(id);
        
        // ⭐⭐ AJOUT CRITIQUE : LOG POUR VÉRIFICATION ⭐⭐
        System.out.println("✅ Enseignant trouvé: " + enseignant.getNom());
        System.out.println("📚 Matière dominante: " + (enseignant.getMatiereDominante() != null ? enseignant.getMatiereDominante().getNom() : "NULL"));
        System.out.println("📚 Matière secondaire: " + (enseignant.getMatiereSecondaire() != null ? enseignant.getMatiereSecondaire().getNom() : "NULL"));
        
        return ResponseEntity.ok(enseignant);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EnseignantDTO> updateEnseignant(@PathVariable String id, 
                                                         @RequestBody EnseignantDTO enseignantDTO) {
        System.out.println("✏️ Mise à jour enseignant ID: " + id);
        
        EnseignantDTO updated = enseignantService.updateEnseignant(id, enseignantDTO);
        
        // ⭐⭐ AJOUT CRITIQUE : LOG POUR VÉRIFICATION ⭐⭐
        System.out.println("✅ Enseignant mis à jour:");
        System.out.println("  - Matière dominante: " + (updated.getMatiereDominante() != null ? updated.getMatiereDominante().getNom() : "null"));
        System.out.println("  - Matière secondaire: " + (updated.getMatiereSecondaire() != null ? updated.getMatiereSecondaire().getNom() : "null"));
        
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnseignant(@PathVariable String id) {
        System.out.println("🗑️ Suppression enseignant ID: " + id);
        
        enseignantService.deleteEnseignant(id);
        return ResponseEntity.noContent().build();
    }
}