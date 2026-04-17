package com.edt.services;

import com.edt.entities.CreneauHoraire;
import com.edt.entities.EmploiDuTemps;
import com.edt.repository.CreneauHoraireRepository;
import com.edt.repository.EmploiDuTempsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmploiDuTempsMatricielService {

    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;
    
    @Autowired
    private EmploiDuTempsRepository emploiDuTempsRepository;

    private static final List<String> JOURS_SEMAINE = Arrays.asList("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI");
    private static final List<String> JOURS_SOIR = Arrays.asList("LUNDI", "MARDI", "JEUDI");

    public Map<String, Object> construireMatricePourClasse(String emploiId, String classeNom) {
        Map<String, Object> matrice = new HashMap<>();
        
        try {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("📊 CONSTRUCTION MATRICE - DÉBUT");
            System.out.println("   Emploi ID: " + emploiId);
            System.out.println("   Classe: " + classeNom);
            System.out.println("=".repeat(80));
            
            // 1. Récupérer l'emploi du temps
            EmploiDuTemps emploi = emploiDuTempsRepository.findById(emploiId)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé"));
            System.out.println("✅ Emploi trouvé: " + emploi.getNom());
            
            // 2. Récupérer tous les créneaux de cet emploi
            List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploiId);
            System.out.println("📅 Créneaux en base: " + creneaux.size());
            
            // 3. Statistiques détaillées
            long coursCount = creneaux.stream().filter(c -> !c.isEstLibre()).count();
            long libresCount = creneaux.size() - coursCount;
            System.out.println("📊 Statistiques:");
            System.out.println("   - Cours (non libres): " + coursCount);
            System.out.println("   - Créneaux libres: " + libresCount);
            
            // Afficher la répartition par jour
            Map<String, Long> parJour = creneaux.stream()
                .filter(c -> !c.isEstLibre())
                .collect(Collectors.groupingBy(CreneauHoraire::getJourSemaine, Collectors.counting()));
            
            System.out.println("   - Répartition par jour:");
            for (String jour : JOURS_SEMAINE) {
                System.out.println("        " + jour + ": " + parJour.getOrDefault(jour, 0L) + " cours");
            }
            
            // 4. Vérifier l'intégrité des données
            Set<String> joursAvecCours = new HashSet<>();
            for (CreneauHoraire c : creneaux) {
                if (!c.isEstLibre()) {
                    joursAvecCours.add(c.getJourSemaine());
                    if (c.getMatiere() == null) {
                        System.err.println("⚠️ Créneau sans matière: " + c.getId());
                    }
                    if (c.getEnseignant() == null) {
                        System.err.println("⚠️ Créneau sans enseignant: " + c.getId());
                    }
                }
            }
            System.out.println("   - Jours avec cours: " + joursAvecCours);
            
            // 5. En-tête
            Map<String, String> entete = new LinkedHashMap<>();
            entete.put("classe", classeNom);
            entete.put("anneeScolaire", emploi.getAnneeScolaire());
            entete.put("dateGeneration", 
                emploi.getDateGeneration() != null ? 
                emploi.getDateGeneration().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : 
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // 6. Définir les horaires (10 créneaux)
            List<Map<String, Object>> lignes = new ArrayList<>();
            
            String[] horaires = {
                "07h00-07h55", "07h55-08h50", "08h50-09h45", "09h45-10h10",
                "10h10-11h05", "11h05-12h00", "12h00-15h00",
                "15h00-15h55", "15h55-16h50", "16h50-17h45"
            };
            
            String[] types = {
                "COURS", "COURS", "COURS", "RECREATION",
                "COURS", "COURS", "GRANDE PAUSE",
                "COURS", "COURS", "COURS"
            };
            
            int[] ordres = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            
            // Créer une map pour un accès rapide aux créneaux
            Map<String, CreneauHoraire> creneauxMap = new HashMap<>();
            for (CreneauHoraire c : creneaux) {
                String key = c.getJourSemaine() + "_" + c.getNumeroCreneau();
                creneauxMap.put(key, c);
            }
            
            // 7. Construire chaque ligne
            for (int i = 0; i < ordres.length; i++) {
                int ordre = ordres[i];
                String horaire = horaires[i];
                String type = types[i];
                
                Map<String, Object> ligne = new LinkedHashMap<>();
                ligne.put("horaire", horaire);
                ligne.put("type", type);
                
                Map<String, Map<String, String>> cellules = new LinkedHashMap<>();
                
                // Pour chaque jour, trouver le créneau correspondant
                for (String jour : JOURS_SEMAINE) {
                    String key = jour + "_" + ordre;
                    CreneauHoraire creneau = creneauxMap.get(key);
                    
                    Map<String, String> cellule = new LinkedHashMap<>();
                    
                    if (creneau != null) {
                        if (creneau.isEstLibre()) {
                            cellule.put("type", "VIDE");
                            cellule.put("contenu", "-");
                        } else {
                            cellule.put("type", "COURS");
                            cellule.put("matiere", creneau.getMatiere() != null ? 
                                        creneau.getMatiere().getCode() : "?");
                            cellule.put("enseignant", creneau.getEnseignant() != null ? 
                                        creneau.getEnseignant().getNom() : "?");
                            cellule.put("classe", creneau.getClasse() != null ? 
                                        creneau.getClasse().getNom() : "");
                            cellule.put("salle", creneau.getSalle() != null ? 
                                        creneau.getSalle() : "");
                        }
                    } else {
                        cellule.put("type", "VIDE");
                        cellule.put("contenu", "-");
                        System.err.println("⚠️ Créneau manquant pour " + jour + " ordre " + ordre);
                    }
                    
                    cellules.put(jour, cellule);
                }
                
                ligne.put("cellules", cellules);
                lignes.add(ligne);
            }
            
            // 8. Compter le nombre de cours dans la matrice
            int coursDansMatrice = 0;
            for (Map<String, Object> ligne : lignes) {
                if (!"RECREATION".equals(ligne.get("type")) && !"GRANDE PAUSE".equals(ligne.get("type"))) {
                    Map<String, Map<String, String>> cellules = (Map<String, Map<String, String>>) ligne.get("cellules");
                    for (String jour : JOURS_SEMAINE) {
                        Map<String, String> cellule = cellules.get(jour);
                        if (cellule != null && "COURS".equals(cellule.get("type"))) {
                            coursDansMatrice++;
                        }
                    }
                }
            }
            
            System.out.println("\n🔍 VÉRIFICATION FINALE:");
            System.out.println("   - Cours en base: " + coursCount);
            System.out.println("   - Cours dans matrice: " + coursDansMatrice);
            
            if (coursCount != coursDansMatrice) {
                System.err.println("❌ INCOHÉRENCE DÉTECTÉE !");
                System.err.println("   Causes possibles:");
                System.err.println("   - Créneaux sans numéro_creneau");
                System.err.println("   - Créneaux avec jour incorrect");
                System.err.println("   - Créneaux manquants pour certains ordres");
                
                // Afficher tous les créneaux pour debug
                System.out.println("\n📋 DÉTAIL DES CRÉNEAUX EN BASE:");
                for (CreneauHoraire c : creneaux) {
                    if (!c.isEstLibre()) {
                        System.out.println("   " + c.getJourSemaine() + " ordre " + c.getNumeroCreneau() + 
                                         " [" + c.getHeureDebut() + "-" + c.getHeureFin() + "]: " +
                                         (c.getMatiere() != null ? c.getMatiere().getCode() : "?") + " " +
                                         (c.getEnseignant() != null ? c.getEnseignant().getNom() : "?"));
                    }
                }
            } else {
                System.out.println("✅ Cohérence vérifiée !");
            }
            
            matrice.put("entete", entete);
            matrice.put("jours", JOURS_SEMAINE);
            matrice.put("lignes", lignes);
            
            System.out.println("\n✅ CONSTRUCTION MATRICE TERMINÉE");
            return matrice;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur construction matrice: " + e.getMessage());
            e.printStackTrace();
            matrice.put("error", e.getMessage());
            return matrice;
        }
    }
}