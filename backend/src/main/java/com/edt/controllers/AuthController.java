// C:\projets\java\edt-generator\backend\src\main\java\com\edt\controllers\AuthController.java
package com.edt.controllers;

import com.edt.dtos.*;
import com.edt.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/demander-connexion")
    public ResponseEntity<DemandeConnexionResultDTO> demanderConnexion(@RequestBody DemandeConnexionDTO demande) {
        try {
            DemandeConnexionResultDTO result = authService.demanderConnexion(demande.getEmail());
            
            // IMPORTANT: On retourne TOUJOURS 200 avec le résultat
            // Même si success=false, on veut que le frontend reçoive la réponse
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // En cas d'erreur technique, on retourne quand même un DTO avec success=false
            DemandeConnexionResultDTO errorResult = new DemandeConnexionResultDTO(
                false, 
                "Erreur serveur: " + e.getMessage()
            );
            return ResponseEntity.ok(errorResult); // 200 au lieu de 400
        }
    }
    
    @PostMapping("/valider-code")
    public ResponseEntity<ValidationCodeResultDTO> validerCode(@RequestBody ValidationCodeDTO validation) {
        try {
            ValidationCodeResultDTO result = authService.validerCode(
                validation.getUtilisateurId(),
                validation.getCode()
            );
            
            // Toujours 200
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            ValidationCodeResultDTO errorResult = new ValidationCodeResultDTO(
                false, 
                "Erreur serveur: " + e.getMessage()
            );
            return ResponseEntity.ok(errorResult);
        }
    }
}