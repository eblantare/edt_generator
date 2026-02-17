package com.edt.controllers;

import com.edt.dtos.MatiereDTO;
import com.edt.services.MatiereService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    
    // NOUVEL endpoint pour la pagination
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
        
        // Log pour déboguer
        System.out.println("✅ Retourne " + matieresPage.getNumberOfElements() + " matières");
        matieresPage.getContent().forEach(m -> 
            System.out.println("  - ID: " + m.getId() + ", Code: " + m.getCode())
        );
        
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
    public ResponseEntity<Void> deleteMatiere(@PathVariable String id) {
        System.out.println("📡 DELETE /api/matieres/" + id);
        matiereService.deleteMatiere(id);
        return ResponseEntity.noContent().build();
    }
}