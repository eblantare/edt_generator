// C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\AuthService.java
package com.edt.services;

import com.edt.dtos.*;
import com.edt.entities.*;
import com.edt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private CodeConnexionRepository codeConnexionRepository;
    
    @Autowired
    private EmailService emailService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final Random RANDOM = new SecureRandom();
    
    /**
     * Étape 1: Demande de connexion avec email - VERSION CORRIGÉE
     */
    public DemandeConnexionResultDTO demanderConnexion(String email) {
        // Validation de base
        if (email == null || email.trim().isEmpty()) {
            return new DemandeConnexionResultDTO(false, "Email requis");
        }
        
        System.out.println("🔐 Demande de connexion pour: " + email);
        
        try {
            // Récupérer l'utilisateur
            Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);
            
            if (utilisateur == null) {
                System.out.println("❌ Email non trouvé: " + email);
                return new DemandeConnexionResultDTO(false, "Email non trouvé");
            }
            
            if (!utilisateur.getEstActif()) {
                System.out.println("❌ Compte inactif: " + email);
                return new DemandeConnexionResultDTO(false, "Compte désactivé");
            }
            
            // SOLUTION 1: Supprimer les anciens codes en NATIVE QUERY
            System.out.println("🧹 Suppression des anciens codes...");
            codeConnexionRepository.deleteByUtilisateurId(utilisateur.getId());
            
            // Forcer la synchronisation avec la base
            codeConnexionRepository.flush();
            entityManager.clear(); // Vide le cache JPA
            
            // Générer un nouveau code
            String code = genererCode();
            System.out.println("📧 Nouveau code généré: " + code);
            
            // Créer le code
            CodeConnexion codeConnexion = new CodeConnexion(utilisateur, code);
            codeConnexion.setDateExpiration(LocalDateTime.now().plusMinutes(10));
            
            // Sauvegarder
            codeConnexion = codeConnexionRepository.save(codeConnexion);
            codeConnexionRepository.flush();
            
            // Envoyer le code
            emailService.envoyerCodeConnexion(utilisateur.getEmail(), code);
            
            return new DemandeConnexionResultDTO(true, "Code envoyé avec succès", utilisateur.getId());
            
        } catch (DataIntegrityViolationException e) {
            System.err.println("⚠️ Erreur de contrainte - tentative de récupération...");
            
            // SOLUTION 2: En cas d'erreur, on force la suppression par SQL natif
            try {
                // Récupérer l'utilisateur
                Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElse(null);
                if (utilisateur != null) {
                    // Supprimer directement en SQL
                    entityManager.createNativeQuery("DELETE FROM codes_connexion WHERE utilisateur_id = ? AND NOT est_utilise")
                        .setParameter(1, utilisateur.getId())
                        .executeUpdate();
                    
                    entityManager.flush();
                    
                    // Réessayer
                    String code = genererCode();
                    CodeConnexion codeConnexion = new CodeConnexion(utilisateur, code);
                    codeConnexion.setDateExpiration(LocalDateTime.now().plusMinutes(10));
                    codeConnexionRepository.save(codeConnexion);
                    codeConnexionRepository.flush();
                    
                    emailService.envoyerCodeConnexion(utilisateur.getEmail(), code);
                    
                    return new DemandeConnexionResultDTO(true, "Code envoyé avec succès (après récupération)", utilisateur.getId());
                }
            } catch (Exception ex) {
                System.err.println("❌ Échec de la récupération: " + ex.getMessage());
            }
            
            return new DemandeConnexionResultDTO(false, "Erreur technique. Veuillez réessayer dans quelques secondes.");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return new DemandeConnexionResultDTO(false, "Erreur technique: " + e.getMessage());
        }
    }
    
    /**
     * Étape 2: Validation du code - VERSION AMÉLIORÉE
     */
    public ValidationCodeResultDTO validerCode(String utilisateurId, String code) {
        try {
            System.out.println("🔐 Validation du code pour l'utilisateur: " + utilisateurId);
            
            if (utilisateurId == null || code == null || code.trim().isEmpty()) {
                return new ValidationCodeResultDTO(false, "Données de validation invalides");
            }
            
            Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            // Chercher le code valide
            CodeConnexion codeConnexion = codeConnexionRepository
                .findByUtilisateurAndCodeAndEstUtiliseFalse(utilisateur, code.trim())
                .orElse(null);
            
            if (codeConnexion == null) {
                System.out.println("❌ Code invalide pour: " + utilisateur.getEmail());
                return new ValidationCodeResultDTO(false, "Code invalide");
            }
            
            // Vérifier si le code est toujours valide
            if (!codeConnexion.estValide()) {
                System.out.println("❌ Code expiré pour: " + utilisateur.getEmail());
                
                // Supprimer le code expiré
                codeConnexionRepository.delete(codeConnexion);
                codeConnexionRepository.flush();
                
                return new ValidationCodeResultDTO(false, "Code expiré. Veuillez demander un nouveau code.");
            }
            
            // Code valide - marquer comme utilisé
            codeConnexion.setEstUtilise(true);
            codeConnexionRepository.save(codeConnexion);
            codeConnexionRepository.flush();
            
            // Mettre à jour la dernière connexion
            utilisateur.setDerniereConnexion(LocalDateTime.now());
            utilisateurRepository.save(utilisateur);
            utilisateurRepository.flush();
            
            // Nettoyer les autres codes non utilisés (au cas où)
            entityManager.createNativeQuery("DELETE FROM codes_connexion WHERE utilisateur_id = ? AND NOT est_utilise")
                .setParameter(1, utilisateur.getId())
                .executeUpdate();
            
            // Créer un token de session
            String token = "TOKEN_" + utilisateur.getId() + "_" + System.currentTimeMillis();
            
            System.out.println("✅ Connexion réussie pour: " + utilisateur.getEmail());
            
            return new ValidationCodeResultDTO(
                true, 
                "Connexion réussie", 
                token, 
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getRole()
            );
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la validation: " + e.getMessage());
            e.printStackTrace();
            return new ValidationCodeResultDTO(false, "Erreur de validation: " + e.getMessage());
        }
    }
    
    /**
     * Génère un code aléatoire de 7 à 9 chiffres
     */
    private String genererCode() {
        int longueur = 7 + RANDOM.nextInt(3);
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < longueur; i++) {
            code.append(RANDOM.nextInt(10));
        }
        
        return code.toString();
    }
}