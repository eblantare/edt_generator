// C:\projets\java\edt-generator\backend\src\main\java\com\edt\controllers\MatiereController.java
package com.edt.controllers;

import com.edt.dtos.MatiereDTO;
import com.edt.services.MatiereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matieres")
@CrossOrigin(origins = "http://localhost:4200")
public class MatiereController {
    
    @Autowired
    private MatiereService matiereService;
    
    // Endpoint existant pour compatibilité
    @GetMapping
    public ResponseEntity<List<MatiereDTO>> getAllMatieres() {
        System.out.println("📡 GET /api/matieres");
        List<MatiereDTO> matieres = matiereService.getAllMatieres();
        System.out.println("✅ Retourne " + matieres.size() + " matières");
        return ResponseEntity.ok(matieres);
    }
    
    // Endpoint pour la pagination
    @GetMapping("/paginated")
    public ResponseEntity<Page<MatiereDTO>> getMatieresPaginated(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "code") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        System.out.println("📡 GET /api/matieres/paginated - page: " + page + 
                          ", size: " + size + ", search: " + search);
        
        Page<MatiereDTO> matieresPage = matiereService.getAllMatieresPaginated(
            page, size, search, sortBy, sortDirection
        );
        
        System.out.println("✅ Retourne " + matieresPage.getNumberOfElements() + " matières");
        
        return ResponseEntity.ok(matieresPage);
    }
    
    @PostMapping
    public ResponseEntity<MatiereDTO> createMatiere(@RequestBody MatiereDTO matiereDTO) {
        System.out.println("📡 POST /api/matieres - Création: " + matiereDTO.getCode());
        MatiereDTO created = matiereService.createMatiere(matiereDTO);
        System.out.println("✅ Matière créée avec ID: " + created.getId());
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MatiereDTO> getMatiere(@PathVariable String id) {
        System.out.println("📡 GET /api/matieres/" + id);
        MatiereDTO matiere = matiereService.getMatiereById(id);
        System.out.println("✅ Matière trouvée: " + matiere.getCode());
        return ResponseEntity.ok(matiere);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MatiereDTO> updateMatiere(@PathVariable String id, 
                                                   @RequestBody MatiereDTO matiereDTO) {
        System.out.println("📡 PUT /api/matieres/" + id + " - Code: " + matiereDTO.getCode());
        MatiereDTO updated = matiereService.updateMatiere(id, matiereDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMatiere(@PathVariable String id) {
        System.out.println("📡 DELETE /api/matieres/" + id);
        
        try {
            matiereService.deleteMatiere(id);
            return ResponseEntity.noContent().build();
            
        } catch (RuntimeException e) {
            if ("IMPOSSIBLE_SUPPRIMER_MATIERE_UTILISEE".equals(e.getMessage())) {
                // Retourner une erreur 400 avec un message clair
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("code", "IMPOSSIBLE_SUPPRIMER");
                errorResponse.put("message", "Cette matière ne peut pas être supprimée car elle est rattachée à un ou plusieurs enseignants.");
                
                System.out.println("❌ Impossible de supprimer - matière utilisée par des enseignants");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Autres erreurs
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", "ERREUR_TECHNIQUE");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}