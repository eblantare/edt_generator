// C:\projets\java\edt-generator\backend\src\main\java\com\edt\controllers\EnseignementController.java
package com.edt.controllers;

import com.edt.dtos.EnseignementDTO;
import com.edt.services.EnseignementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enseignements")
@CrossOrigin(origins = "http://localhost:4200")
public class EnseignementController {
    
    @Autowired
    private EnseignementService enseignementService;
    
    // === CRUD OPERATIONS ===
    
    @PostMapping
    public ResponseEntity<EnseignementDTO> createEnseignement(@RequestBody EnseignementDTO dto) {
        try {
            EnseignementDTO created = enseignementService.createEnseignement(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EnseignementDTO> getEnseignement(@PathVariable String id) {
        try {
            EnseignementDTO dto = enseignementService.getEnseignementById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EnseignementDTO> updateEnseignement(@PathVariable String id, @RequestBody EnseignementDTO dto) {
        try {
            dto.setId(id);
            EnseignementDTO updated = enseignementService.updateEnseignement(dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnseignement(@PathVariable String id) {
        try {
            enseignementService.deleteEnseignement(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // === LIST OPERATIONS ===
    
    @GetMapping
    public ResponseEntity<Page<EnseignementDTO>> getAllEnseignements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        
        System.out.println("====================");
        System.out.println("📥 API /api/enseignements appelée");
        System.out.println("📋 Paramètres:");
        System.out.println("  - page: " + page);
        System.out.println("  - size: " + size);
        System.out.println("  - search: '" + search + "'");
        System.out.println("  - sortBy: '" + sortBy + "'");
        System.out.println("  - sortDirection: '" + sortDirection + "'");
        
        try {
            Page<EnseignementDTO> result = enseignementService.getAllEnseignements(page, size, search, sortBy, sortDirection);
            System.out.println("📤 Réponse envoyée:");
            System.out.println("  - Nombre d'éléments: " + result.getNumberOfElements());
            System.out.println("  - Page actuelle: " + (result.getNumber() + 1) + "/" + result.getTotalPages());
            System.out.println("  - Total éléments: " + result.getTotalElements());
            System.out.println("  - Tri appliqué: " + sortBy + " " + sortDirection);
            System.out.println("====================");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des enseignements: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<EnseignementDTO>> getEnseignementsByEnseignant(@PathVariable String enseignantId) {
        try {
            List<EnseignementDTO> enseignements = enseignementService.getEnseignementsByEnseignant(enseignantId);
            return ResponseEntity.ok(enseignements);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<EnseignementDTO>> getEnseignementsByClasse(@PathVariable String classeId) {
        try {
            List<EnseignementDTO> enseignements = enseignementService.getEnseignementsByClasse(classeId);
            return ResponseEntity.ok(enseignements);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/matiere/{matiereId}")
    public ResponseEntity<List<EnseignementDTO>> getEnseignementsByMatiere(@PathVariable String matiereId) {
        try {
            List<EnseignementDTO> enseignements = enseignementService.getEnseignementsByMatiere(matiereId);
            return ResponseEntity.ok(enseignements);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // === STATISTICS ===
    
    @GetMapping("/stats/heures-enseignant/{enseignantId}")
    public ResponseEntity<Integer> getTotalHeuresByEnseignant(@PathVariable String enseignantId) {
        try {
            Integer totalHeures = enseignementService.getTotalHeuresByEnseignant(enseignantId);
            return ResponseEntity.ok(totalHeures);
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }
    
    @GetMapping("/stats/heures-classe/{classeId}")
    public ResponseEntity<Integer> getTotalHeuresByClasse(@PathVariable String classeId) {
        try {
            Integer totalHeures = enseignementService.getTotalHeuresByClasse(classeId);
            return ResponseEntity.ok(totalHeures);
        } catch (Exception e) {
            return ResponseEntity.ok(0);
        }
    }
}