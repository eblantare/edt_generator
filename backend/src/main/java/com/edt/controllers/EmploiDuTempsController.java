// C:\projets\java\edt-generator\backend\src\main\java\com\edt\controllers\EmploiDuTempsController.java
package com.edt.controllers;

import com.edt.dtos.*;
import com.edt.services.ConfigurationService;
import com.edt.services.GenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emplois-du-temps")
@CrossOrigin(origins = "http://localhost:4200")
public class EmploiDuTempsController {
    
    @Autowired
    private ConfigurationService configurationService;
    
    @Autowired
    private GenerationService generationService;
    
    // === CONFIGURATION DES HORAIRES ===
    
    @GetMapping("/configuration/horaires")
    public ResponseEntity<List<HoraireJournalierDTO>> getHoraires() {
        return ResponseEntity.ok(configurationService.getHorairesJournaliers());
    }
    
    @GetMapping("/configuration/horaires/{jour}")
    public ResponseEntity<HoraireJournalierDTO> getHoraireParJour(@PathVariable String jour) {
        return ResponseEntity.ok(configurationService.getHoraireParJour(jour));
    }
    
    @PostMapping("/configuration/horaires")
    public ResponseEntity<HoraireJournalierDTO> sauvegarderHoraire(@RequestBody HoraireJournalierDTO dto) {
        return ResponseEntity.ok(configurationService.sauvegarderHoraire(dto));
    }
    
    @PutMapping("/configuration/horaires/{id}")
    public ResponseEntity<HoraireJournalierDTO> updateHoraire(@PathVariable String id, @RequestBody HoraireJournalierDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(configurationService.updateHoraire(dto));
    }
    
    @DeleteMapping("/configuration/horaires/{id}")
    public ResponseEntity<Void> deleteHoraire(@PathVariable String id) {
        configurationService.deleteHoraire(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/configuration/horaires/initialiser")
    public ResponseEntity<Void> initialiserHorairesParDefaut() {
        configurationService.initialiserHorairesParDefaut();
        return ResponseEntity.ok().build();
    }
    
    // === ATTRIBUTION DES ENSEIGNEMENTS ===
    
    @PostMapping("/configuration/enseignements")
    public ResponseEntity<EnseignementDTO> attribuerEnseignement(@RequestBody EnseignementDTO dto) {
        return ResponseEntity.ok(configurationService.attribuerEnseignement(dto));
    }
    
    @GetMapping("/configuration/enseignements")
    public ResponseEntity<List<EnseignementDTO>> getAllEnseignements() {
        return ResponseEntity.ok(configurationService.getAllEnseignements());
    }
    
    @GetMapping("/configuration/enseignements/enseignant/{enseignantId}")
    public ResponseEntity<List<EnseignementDTO>> getEnseignementsParEnseignant(@PathVariable String enseignantId) {
        return ResponseEntity.ok(configurationService.getEnseignementsParEnseignant(enseignantId));
    }
    
    @GetMapping("/configuration/enseignements/classe/{classeId}")
    public ResponseEntity<List<EnseignementDTO>> getEnseignementsParClasse(@PathVariable String classeId) {
        return ResponseEntity.ok(configurationService.getEnseignementsParClasse(classeId));
    }
    
    @GetMapping("/configuration/enseignements/matiere/{matiereId}")
    public ResponseEntity<List<EnseignementDTO>> getEnseignementsParMatiere(@PathVariable String matiereId) {
        return ResponseEntity.ok(configurationService.getEnseignementsParMatiere(matiereId));
    }
    
    @PutMapping("/configuration/enseignements/{id}")
    public ResponseEntity<EnseignementDTO> updateEnseignement(@PathVariable String id, @RequestBody EnseignementDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(configurationService.updateEnseignement(dto));
    }
    
    @DeleteMapping("/configuration/enseignements/{id}")
    public ResponseEntity<Void> deleteEnseignement(@PathVariable String id) {
        configurationService.deleteEnseignement(id);
        return ResponseEntity.noContent().build();
    }
    
    // === GÉNÉRATION DES EMPLOIS DU TEMPS ===
    
    @PostMapping("/generer/global")
    public ResponseEntity<EmploiDuTempsDTO> genererEmploiDuTempsGlobal(@RequestParam String anneeScolaire) {
        return ResponseEntity.ok(generationService.genererEmploiDuTempsGlobal(anneeScolaire));
    }
    
    @PostMapping("/generer/classe/{classeId}")
    public ResponseEntity<EmploiDuTempsDTO> genererEmploiDuTempsClasse(
            @PathVariable String classeId, 
            @RequestParam String anneeScolaire) {
        return ResponseEntity.ok(generationService.genererEmploiDuTempsClasse(classeId, anneeScolaire));
    }
    
    @PostMapping("/generer/enseignant/{enseignantId}")
    public ResponseEntity<EmploiDuTempsDTO> genererEmploiDuTempsEnseignant(
            @PathVariable String enseignantId,
            @RequestParam String anneeScolaire) {
        return ResponseEntity.ok(generationService.genererEmploiDuTempsEnseignant(enseignantId, anneeScolaire));
    }
    
    // === GESTION DES EMPLOIS DU TEMPS ===
    
    @GetMapping("/emplois")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploisDuTemps() {
        return ResponseEntity.ok(generationService.getAllEmploisDuTemps());
    }
    
    @GetMapping("/emplois/global")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploisGlobal() {
        return ResponseEntity.ok(generationService.getEmploisGlobal());
    }
    
    @GetMapping("/emplois/classe/{classeId}")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploisParClasse(@PathVariable String classeId) {
        return ResponseEntity.ok(generationService.getEmploisParClasse(classeId));
    }
    
    @GetMapping("/emplois/{id}")
    public ResponseEntity<EmploiDuTempsDTO> getEmploiDuTemps(@PathVariable String id) {
        return ResponseEntity.ok(generationService.getEmploiDuTemps(id));
    }
    
    @PutMapping("/emplois/{id}/statut")
    public ResponseEntity<EmploiDuTempsDTO> updateStatutEmploi(
            @PathVariable String id,
            @RequestParam String statut) {
        return ResponseEntity.ok(generationService.updateStatutEmploi(id, statut));
    }
    
    @DeleteMapping("/emplois/{id}")
    public ResponseEntity<Void> deleteEmploiDuTemps(@PathVariable String id) {
        generationService.deleteEmploiDuTemps(id);
        return ResponseEntity.noContent().build();
    }
    
    // === GESTION DES CRÉNEAUX ===
    
    @GetMapping("/creneaux/emploi/{emploiId}")
    public ResponseEntity<List<CreneauHoraireDTO>> getCreneauxParEmploi(@PathVariable String emploiId) {
        return ResponseEntity.ok(generationService.getCreneauxParEmploi(emploiId));
    }
    
    @GetMapping("/creneaux/enseignant/{enseignantId}")
    public ResponseEntity<List<CreneauHoraireDTO>> getCreneauxParEnseignant(@PathVariable String enseignantId) {
        return ResponseEntity.ok(generationService.getCreneauxParEnseignant(enseignantId));
    }
    
    @GetMapping("/creneaux/classe/{classeId}")
    public ResponseEntity<List<CreneauHoraireDTO>> getCreneauxParClasse(@PathVariable String classeId) {
        return ResponseEntity.ok(generationService.getCreneauxParClasse(classeId));
    }
    
    @GetMapping("/creneaux/jour/{emploiId}/{jour}")
    public ResponseEntity<List<CreneauHoraireDTO>> getCreneauxParJour(
            @PathVariable String emploiId,
            @PathVariable String jour) {
        return ResponseEntity.ok(generationService.getCreneauxParJour(emploiId, jour));
    }
    
    @PutMapping("/creneaux/{id}")
    public ResponseEntity<CreneauHoraireDTO> updateCreneau(@PathVariable String id, @RequestBody CreneauHoraireDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(generationService.updateCreneau(dto));
    }
    
    @PostMapping("/creneaux/affecter")
    public ResponseEntity<CreneauHoraireDTO> affecterCreneau(@RequestBody AffectationCreneauDTO affectation) {
        return ResponseEntity.ok(generationService.affecterCreneau(affectation));
    }
    
    @PostMapping("/creneaux/liberer/{creneauId}")
    public ResponseEntity<CreneauHoraireDTO> libererCreneau(@PathVariable String creneauId) {
        return ResponseEntity.ok(generationService.libererCreneau(creneauId));
    }
    
    // === STATISTIQUES ET RAPPORTS ===
    
    @GetMapping("/statistiques/global/{emploiId}")
    public ResponseEntity<StatistiquesEmploiDTO> getStatistiquesEmploi(@PathVariable String emploiId) {
        return ResponseEntity.ok(generationService.getStatistiquesEmploi(emploiId));
    }
    
    @GetMapping("/statistiques/enseignant/{enseignantId}")
    public ResponseEntity<StatistiquesEnseignantDTO> getStatistiquesEnseignant(@PathVariable String enseignantId) {
        return ResponseEntity.ok(generationService.getStatistiquesEnseignant(enseignantId));
    }
    
    @GetMapping("/rapports/export/{emploiId}")
    public ResponseEntity<byte[]> exporterEmploiDuTemps(@PathVariable String emploiId,
                                                        @RequestParam String format) {
        byte[] export = generationService.exporterEmploiDuTemps(emploiId, format);
        return ResponseEntity.ok()
                .header("Content-Type", getContentType(format))
                .header("Content-Disposition", "attachment; filename=\"emploi-du-temps." + format.toLowerCase() + "\"")
                .body(export);
    }
    
    private String getContentType(String format) {
        switch (format.toUpperCase()) {
            case "PDF": return "application/pdf";
            case "EXCEL": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "CSV": return "text/csv";
            default: return "application/octet-stream";
        }
    }
}