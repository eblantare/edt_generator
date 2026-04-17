// C:\projets\java\edt-generator\backend\src\main\java\com\edt\controllers\GenerationController.java
package com.edt.controllers;

import com.edt.dtos.GenerationOptionsDTO;
import com.edt.dtos.GenerationRequestDTO;
import com.edt.dtos.GenerationResultDTO;
import com.edt.services.GenerationService;
import com.edt.entities.Classe;
import com.edt.entities.EmploiDuTemps;
import com.edt.entities.Enseignement;
import com.edt.repository.ClasseRepository;
import com.edt.repository.EmploiDuTempsRepository;
import com.edt.repository.EnseignantRepository;
import com.edt.repository.EnseignementRepository;
import com.edt.repository.MatiereRepository;
import com.edt.repository.CreneauHoraireRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/generation")
@CrossOrigin(origins = "http://localhost:4200")
public class GenerationController {
    
    @Autowired
    private GenerationService generationService;
    
    @Autowired
    private ClasseRepository classeRepository;
    
    @Autowired
    private EnseignementRepository enseignementRepository;
    
    @Autowired
    private EmploiDuTempsRepository emploiDuTempsRepository;
    
    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    @Autowired
    private EnseignantRepository enseignantRepository;
    
    // ========== GESTION DES ANNÉES SCOLAIRES ==========
    
    @GetMapping("/annee-scolaire-courante")
    public ResponseEntity<String> getAnneeScolaireCourante() {
        try {
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int month = now.getMonthValue();
            
            String anneeScolaire;
            if (month >= 9) {
                anneeScolaire = year + "-" + (year + 1);
            } else {
                anneeScolaire = (year - 1) + "-" + year;
            }
            
            System.out.println("📅 Année scolaire courante: " + anneeScolaire);
            return ResponseEntity.ok(anneeScolaire);
        } catch (Exception e) {
            return ResponseEntity.ok("2025-2026");
        }
    }
    
    @GetMapping("/annees-scolaires")
    public ResponseEntity<List<String>> getAnneesScolaires() {
        try {
            List<String> annees = generationService.getAnneesScolaires();
            return ResponseEntity.ok(annees);
        } catch (Exception e) {
            return ResponseEntity.ok(Arrays.asList("2025-2026"));
        }
    }
    
    // ========== GÉNÉRATION DES EMPLOIS DU TEMPS ==========
    
    @PostMapping("/global")
    public ResponseEntity<GenerationResultDTO> genererGlobal(@RequestBody GenerationRequestDTO request) {
        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🚀 GÉNÉRATION GLOBALE DEMANDÉE");
            System.out.println("📅 Année scolaire: " + request.getAnneeScolaire());
            System.out.println("=".repeat(60));
            
            long nbEnseignements = enseignementRepository.count();
            if (nbEnseignements == 0) {
                return ResponseEntity.badRequest().body(
                    new GenerationResultDTO(false, "❌ Aucun enseignement trouvé dans la base")
                );
            }
            
            // Créer des options par défaut si non fournies
            GenerationOptionsDTO options = request.getOptions();
            if (options == null) {
                options = new GenerationOptionsDTO();
                options.setVerifierConflits(true);
                options.setOptimiserRepartition(true);
                options.setGenererSalles(false);
                options.setRespecterContraintesEPS(true);
                options.setPlacerPauses(true);
            }
            
            // Appeler la méthode sans chevauchement
            GenerationResultDTO result = generationService.genererGlobalSansChevauchement(request.getAnneeScolaire(), options);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                new GenerationResultDTO(false, "Erreur: " + e.getMessage())
            );
        }
    }
    
    @PostMapping("/classe")
    public ResponseEntity<GenerationResultDTO> genererPourClasse(@RequestBody GenerationRequestDTO request) {
        try {
            System.out.println("\n" + "🔥".repeat(60));
            System.out.println("🔥 GÉNÉRATION POUR CLASSE");
            System.out.println("🔥 ID Classe: " + request.getClasseId());
            System.out.println("🔥 Année: " + request.getAnneeScolaire());
            System.out.println("🔥".repeat(60));
            
            // Vérifications
            if (request.getClasseId() == null || request.getClasseId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new GenerationResultDTO(false, "ID de classe non fourni")
                );
            }
            
            Optional<Classe> classeOpt = classeRepository.findById(request.getClasseId());
            if (classeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new GenerationResultDTO(false, "Classe non trouvée: " + request.getClasseId())
                );
            }
            
            Classe classe = classeOpt.get();
            System.out.println("🏫 Classe: " + classe.getNom());
            
            List<Enseignement> enseignements = enseignementRepository.findByClasseId(classe.getId());
            if (enseignements.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    new GenerationResultDTO(false, "❌ Aucun enseignement pour la classe " + classe.getNom())
                );
            }
            
            System.out.println("📚 " + enseignements.size() + " enseignements trouvés");
            
            // CORRECTION: Créer des options par défaut si non fournies
            GenerationOptionsDTO options = request.getOptions();
            if (options == null) {
                options = new GenerationOptionsDTO();
                options.setVerifierConflits(true);
                options.setOptimiserRepartition(true);
                options.setGenererSalles(false);
                options.setRespecterContraintesEPS(true);
                options.setPlacerPauses(true);
            }
            
            // Toujours passer l'objet options
            GenerationResultDTO result = generationService.genererPourClasse(
                request.getClasseId(), 
                request.getAnneeScolaire(),
                options
            );
            
            if (result.isSuccess()) {
                System.out.println("✅ SUCCÈS ! Emploi généré: " + result.getEmploiDuTempsId());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                new GenerationResultDTO(false, "Erreur: " + e.getMessage())
            );
        }
    }
    
    @PostMapping("/enseignant")
    public ResponseEntity<GenerationResultDTO> genererPourEnseignant(@RequestBody GenerationRequestDTO request) {
        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🚀 GÉNÉRATION POUR ENSEIGNANT");
            System.out.println("🆔 ID Enseignant: " + request.getEnseignantId());
            System.out.println("📅 Année: " + request.getAnneeScolaire());
            System.out.println("=".repeat(60));
            
            // CORRECTION: Créer des options par défaut si non fournies
            GenerationOptionsDTO options = request.getOptions();
            if (options == null) {
                options = new GenerationOptionsDTO();
                options.setVerifierConflits(true);
                options.setOptimiserRepartition(true);
                options.setGenererSalles(false);
                options.setRespecterContraintesEPS(true);
                options.setPlacerPauses(true);
            }
            
            // Toujours passer l'objet options
            GenerationResultDTO result = generationService.genererPourEnseignant(
                request.getEnseignantId(), 
                request.getAnneeScolaire(),
                options
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                new GenerationResultDTO(false, "Erreur: " + e.getMessage())
            );
        }
    }
    
    // ========== HISTORIQUE ET STATUT ==========
    
    @GetMapping("/historique")
    public ResponseEntity<?> getHistorique() {
        try {
            System.out.println("📜 Récupération de l'historique");
            return ResponseEntity.ok(generationService.getHistoriqueGenerations());
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @GetMapping("/statut/{id}")
    public ResponseEntity<?> getStatutGeneration(@PathVariable String id) {
        try {
            System.out.println("📊 Statut pour: " + id);
            return ResponseEntity.ok(generationService.getStatutGeneration(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/emploi/{id}")
    public ResponseEntity<?> supprimerEmploi(@PathVariable String id) {
        try {
            System.out.println("🗑️ Suppression de l'emploi: " + id);
            
            // Vérifier si l'emploi existe
            Optional<EmploiDuTemps> emploiOpt = emploiDuTempsRepository.findById(id);
            if (emploiOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false, 
                    "message", "Emploi du temps non trouvé avec l'ID: " + id
                ));
            }
            
            generationService.deleteEmploiDuTemps(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Emploi du temps supprimé avec succès"
            ));
            
        } catch (Exception e) {
            // Logger l'erreur complète
            System.err.println("❌ ERREUR DÉTAILLÉE: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "Erreur: " + e.getMessage()
            ));
        }
    }
    
    // ========== DIAGNOSTIC ==========
    
    @GetMapping("/diagnostic/base")
    public ResponseEntity<?> diagnosticBase() {
        try {
            Map<String, Object> diagnostic = new LinkedHashMap<>();
            diagnostic.put("timestamp", LocalDateTime.now().toString());
            diagnostic.put("classes", classeRepository.count());
            diagnostic.put("enseignants", enseignantRepository.count());
            diagnostic.put("matieres", matiereRepository.count());
            diagnostic.put("enseignements", enseignementRepository.count());
            diagnostic.put("emplois_du_temps", emploiDuTempsRepository.count());
            diagnostic.put("creneaux_horaires", creneauHoraireRepository.count());
            diagnostic.put("status", "OK");
            return ResponseEntity.ok(diagnostic);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }
    
    @GetMapping("/diagnostic/enseignements")
    public ResponseEntity<?> diagnosticEnseignements() {
        try {
            List<Map<String, Object>> resultats = new ArrayList<>();
            List<Enseignement> enseignements = enseignementRepository.findAll();
            
            for (Enseignement e : enseignements) {
                Map<String, Object> ensMap = new HashMap<>();
                ensMap.put("id", e.getId());
                ensMap.put("classe", e.getClasse() != null ? e.getClasse().getNom() : "N/A");
                ensMap.put("enseignant", e.getEnseignant() != null ? e.getEnseignant().getNom() : "N/A");
                ensMap.put("matiere", e.getMatiere() != null ? e.getMatiere().getCode() : "N/A");
                ensMap.put("heures", e.getHeuresParSemaine());
                resultats.add(ensMap);
            }
            
            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}