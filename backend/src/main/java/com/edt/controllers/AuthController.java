package com.edt.controllers;

import com.edt.dtos.*;
import com.edt.entities.Utilisateur;
import com.edt.repository.UtilisateurRepository;
import com.edt.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;  // AJOUTEZ CETTE LIGNE
    
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
    
    @PostMapping("/inscription")
    public ResponseEntity<InscriptionResultDTO> inscription(@RequestBody InscriptionRequestDTO demande) {
        try {
            System.out.println("📝 Demande d'inscription pour: " + demande.getEmail());
            
            // Vérifier si l'email existe déjà
            if (utilisateurRepository.findByEmail(demande.getEmail()).isPresent()) {
                return ResponseEntity.ok(new InscriptionResultDTO(
                    false, "Cet email est déjà utilisé"
                ));
            }

            // Créer le nouvel utilisateur
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId(UUID.randomUUID().toString());
            utilisateur.setEmail(demande.getEmail());
            utilisateur.setRole(demande.getRole() != null ? demande.getRole() : "CONSULTANT");
            utilisateur.setEstActif(true);
            utilisateur.setCreatedAt(LocalDateTime.now());
            utilisateur.setUpdatedAt(LocalDateTime.now());

            utilisateur = utilisateurRepository.save(utilisateur);

            System.out.println("✅ Utilisateur créé: " + utilisateur.getEmail() + " avec rôle: " + utilisateur.getRole());

            return ResponseEntity.ok(new InscriptionResultDTO(
                true, 
                "Inscription réussie",
                utilisateur.getId()
            ));

        } catch (Exception e) {
            System.err.println("❌ Erreur inscription: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new InscriptionResultDTO(
                false, "Erreur lors de l'inscription: " + e.getMessage()
            ));
        }
    }
}