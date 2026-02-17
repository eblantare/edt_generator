// C:\projets\java\edt-generator\backend\src\main\java\com\edt\controllers\DiagnosticController.java
package com.edt.controllers;

import com.edt.services.GenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
@CrossOrigin(origins = "http://localhost:4200")
public class DiagnosticController {
    
    @Autowired
    private GenerationService generationService;
    
    @GetMapping("/base-donnees")
    public ResponseEntity<Map<String, Object>> diagnostiquerBase() {
        try {
            System.out.println("🔍 Diagnostic de la base de données demandé");
            Map<String, Object> diagnostic = generationService.diagnostiquerBaseDeDonnees();
            return ResponseEntity.ok(diagnostic);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du diagnostic: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "statut", "ERREUR",
                "message", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
        }
    }
}