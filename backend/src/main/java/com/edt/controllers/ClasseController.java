// C:\projets\java\edt-generator\backend\src\main\java\com\edt\controllers\ClasseController.java
package com.edt.controllers;

import com.edt.dtos.ClasseDTO;
import com.edt.services.ClasseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@CrossOrigin(origins = "http://localhost:4200")
public class ClasseController {
    
    @Autowired
    private ClasseService classeService;
    
    // Endpoint existant pour compatibilité
    @GetMapping
    public ResponseEntity<List<ClasseDTO>> getAllClasses() {
        System.out.println("📡 GET /api/classes");
        List<ClasseDTO> classes = classeService.getAllClasses();
        System.out.println("✅ Retourne " + classes.size() + " classes");
        return ResponseEntity.ok(classes);
    }
    
    // NOUVEL endpoint pour la pagination
    @GetMapping("/paginated")
    public ResponseEntity<Page<ClasseDTO>> getClassesPaginated(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "nom") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        System.out.println("📡 GET /api/classes/paginated - page: " + page + 
                          ", size: " + size + ", search: " + search);
        
        Page<ClasseDTO> classesPage = classeService.getAllClassesPaginated(
            page, size, search, sortBy, sortDirection
        );
        
        // Log pour déboguer
        System.out.println("✅ Retourne " + classesPage.getNumberOfElements() + " classes");
        classesPage.getContent().forEach(c -> 
            System.out.println("  - ID: " + c.getId() + ", Nom: " + c.getNom())
        );
        
        return ResponseEntity.ok(classesPage);
    }
    
    @PostMapping
    public ResponseEntity<ClasseDTO> createClasse(@RequestBody ClasseDTO classeDTO) {
        System.out.println("📡 POST /api/classes - Création: " + classeDTO.getNom());
        ClasseDTO created = classeService.createClasse(classeDTO);
        System.out.println("✅ Classe créée avec ID: " + created.getId());
        return ResponseEntity.ok(created);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ClasseDTO> getClasse(@PathVariable String id) {
        System.out.println("📡 GET /api/classes/" + id);
        ClasseDTO classe = classeService.getClasseById(id);
        System.out.println("✅ Classe trouvée: " + classe.getNom());
        return ResponseEntity.ok(classe);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ClasseDTO> updateClasse(@PathVariable String id, 
                                                   @RequestBody ClasseDTO classeDTO) {
        System.out.println("📡 PUT /api/classes/" + id + " - Nom: " + classeDTO.getNom());
        ClasseDTO updated = classeService.updateClasse(id, classeDTO);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClasse(@PathVariable String id) {
       System.out.println("📡 DELETE /api/classes/" + id);
       try {
          classeService.deleteClasse(id);
          return ResponseEntity.noContent().build();
       } catch (RuntimeException e) {
        // Retourner le message d'erreur avec un statut 400 (Bad Request)
          return ResponseEntity.badRequest().body(e.getMessage());
      }
   }
}