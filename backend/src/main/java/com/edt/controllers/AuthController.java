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
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            DemandeConnexionResultDTO errorResult = new DemandeConnexionResultDTO(
                false, 
                "Erreur serveur: " + e.getMessage()
            );
            return ResponseEntity.ok(errorResult);
        }
    }
    
    @PostMapping("/verifier-email")
    public ResponseEntity<VerificationEmailResultDTO> verifierEmail(@RequestBody VerificationEmailDTO verification) {
        try {
            VerificationEmailResultDTO result = authService.verifierEmail(verification.getEmail());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            VerificationEmailResultDTO errorResult = new VerificationEmailResultDTO(
                false, 
                "Erreur serveur: " + e.getMessage()
            );
            return ResponseEntity.ok(errorResult);
        }
    }
    
    @PostMapping("/inscrire")
    public ResponseEntity<InscriptionResultDTO> inscrire(@RequestBody InscriptionDTO inscription) {
        try {
            InscriptionResultDTO result = authService.inscrire(inscription);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            InscriptionResultDTO errorResult = new InscriptionResultDTO(
                false, 
                "Erreur serveur: " + e.getMessage()
            );
            return ResponseEntity.ok(errorResult);
        }
    }
    
    @PostMapping("/valider-code")
    public ResponseEntity<ValidationCodeResultDTO> validerCode(@RequestBody ValidationCodeDTO validation) {
        try {
            ValidationCodeResultDTO result = authService.validerCode(
                validation.getUtilisateurId(),
                validation.getCode()
            );
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