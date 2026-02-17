// C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\EmploiDuTempsMatricielService.java
package com.edt.services;

import com.edt.entities.CreneauHoraire;
import com.edt.entities.EmploiDuTemps;
import com.edt.entities.Matiere;
import com.edt.repository.CreneauHoraireRepository;
import com.edt.repository.EmploiDuTempsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EmploiDuTempsMatricielService {

    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;
    
    @Autowired
    private EmploiDuTempsRepository emploiDuTempsRepository;

    // Jours fixes de la semaine
    private static final List<String> JOURS = Arrays.asList(
        "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI"
    );
    
    // Jours avec cours du soir
    private static final List<String> JOURS_SOIR = Arrays.asList("LUNDI", "MARDI", "JEUDI");

    // Créneaux horaires FIXES avec leurs numéros
    private static final List<Map<String, Object>> CRENEAUX = Arrays.asList(
        createCreneau(1, "07h00-07h55", "COURS"),
        createCreneau(2, "07h55-08h50", "COURS"),
        createCreneau(3, "08h50-09h45", "COURS"),
        createCreneau(4, "09h45-10h10", "RECREATION"),
        createCreneau(5, "10h10-11h05", "COURS"),
        createCreneau(6, "11h05-12h00", "COURS"),
        createCreneau(7, "12h00-15h00", "GRANDE PAUSE"),
        createCreneau(8, "15h00-15h55", "COURS"),
        createCreneau(9, "15h55-16h50", "COURS"),
        createCreneau(10, "16h50-17h45", "COURS")
    );

    private static Map<String, Object> createCreneau(int numero, String horaire, String type) {
        Map<String, Object> creneau = new HashMap<>();
        creneau.put("numero", numero);
        creneau.put("horaire", horaire);
        creneau.put("type", type);
        return creneau;
    }

    /**
     * Construit une matrice [Créneaux × Jours] pour une classe
     */
    public Map<String, Object> construireMatricePourClasse(String emploiId, String classeNom) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        // 1. Récupérer l'emploi du temps
        EmploiDuTemps emploi = emploiDuTempsRepository.findById(emploiId).orElse(null);
        if (emploi == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Emploi du temps non trouvé");
            return Collections.singletonMap("error", error);
        }
        
        // 2. En-tête : informations générales
        Map<String, String> entete = new LinkedHashMap<>();
        entete.put("classe", classeNom);
        entete.put("anneeScolaire", emploi.getAnneeScolaire() != null ? emploi.getAnneeScolaire() : "2025-2026");
        entete.put("dateGeneration", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        result.put("entete", entete);

        // 3. Ligne des jours
        result.put("jours", JOURS);

        // 4. Récupérer tous les créneaux de cet emploi du temps
        List<CreneauHoraire> tousCreneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploiId);
        
        // 5. Construire un index pour accès rapide : [jour][numero] = CreneauHoraire
        Map<String, Map<Integer, CreneauHoraire>> indexCreneaux = new HashMap<>();
        for (String jour : JOURS) {
            indexCreneaux.put(jour, new HashMap<>());
        }
        
        for (CreneauHoraire c : tousCreneaux) {
            String jour = c.getJourSemaine();
            int numero = c.getNumeroCreneau() != null ? c.getNumeroCreneau() : 0;
            if (indexCreneaux.containsKey(jour)) {
                indexCreneaux.get(jour).put(numero, c);
            }
        }

        // 6. Construire les lignes du tableau
        List<Map<String, Object>> lignes = new ArrayList<>();
        
        for (Map<String, Object> creneauDef : CRENEAUX) {
            int numero = (int) creneauDef.get("numero");
            String horaire = (String) creneauDef.get("horaire");
            String type = (String) creneauDef.get("type");
            
            Map<String, Object> ligne = new LinkedHashMap<>();
            ligne.put("horaire", horaire);
            ligne.put("type", type);
            ligne.put("numero", numero);
            
            // Cellules pour chaque jour
            Map<String, Map<String, String>> cellules = new LinkedHashMap<>();
            
            for (String jour : JOURS) {
                Map<String, String> cellule = new LinkedHashMap<>();
                
                // Vérifier si c'est une pause
                if ("RECREATION".equals(type) || "GRANDE PAUSE".equals(type)) {
                    cellule.put("type", "PAUSE");
                    cellule.put("libelle", type);
                    cellule.put("valeur", type);
                } 
                // Vérifier si c'est un créneau du soir non autorisé
                else if (numero >= 8 && !JOURS_SOIR.contains(jour)) {
                    cellule.put("type", "VIDE");
                    cellule.put("libelle", "-");
                    cellule.put("valeur", "");
                }
                // Sinon, chercher un cours
                else {
                    CreneauHoraire cours = indexCreneaux.get(jour).get(numero);
                    
                    if (cours != null && !cours.isEstLibre()) {
                        // Format abrégé de la matière (code)
                        String matiereCode = cours.getMatiere() != null ? 
                            getMatiereAbregee(cours.getMatiere()) : "";
                        
                        // Format abrégé de l'enseignant (M. NOM)
                        String enseignantNom = cours.getEnseignant() != null ? 
                          cours.getEnseignant().getNom() : "";
                        
                        // Nettoyer le nom (enlever les espaces et titres)
                        enseignantNom = enseignantNom.replace("M.", "").replace("M. ", "").trim();
                        
                        // Préparer la valeur pour l'affichage (mais on va utiliser deux lignes)
                        String valeur = matiereCode + " " + enseignantNom;
                        cellule.put("type", "COURS");
                        cellule.put("matiere", matiereCode);
                        cellule.put("enseignant", enseignantNom);
                        cellule.put("salle", cours.getSalle() != null ? cours.getSalle() : "");
                        cellule.put("valeur", valeur.trim());
                        cellule.put("id", cours.getId());
                    } else {
                        cellule.put("type", "VIDE");
                        cellule.put("libelle", "-");
                        cellule.put("valeur", "");
                    }
                }
                
                cellules.put(jour, cellule);
            }
            
            ligne.put("cellules", cellules);
            lignes.add(ligne);
        }

        result.put("lignes", lignes);
        result.put("totalCreneaux", tousCreneaux.size());
        result.put("creneauxOccupes", tousCreneaux.stream().filter(c -> !c.isEstLibre()).count());
        
        return result;
    }
    
    /**
     * Retourne le code abrégé d'une matière
     */
    /**
    * Retourne le code abrégé d'une matière et le nom de l'enseignant sans "M."
    */
    private String getMatiereAbregee(Matiere matiere) {
        if (matiere == null) return "";
    
       // Utiliser le code s'il existe
       if (matiere.getCode() != null && !matiere.getCode().isEmpty()) {
           return matiere.getCode();
       }
    
       // Sinon, générer une abréviation à partir du nom
       String nom = matiere.getNom().toUpperCase();
       if (nom.contains("MATHEMATIQUES")) return "MATH";
       if (nom.contains("FRANCAIS")) return "FRAN";
       if (nom.contains("ANGLAIS")) return "ANG";
       if (nom.contains("PHYSIQUE")) return "PCT";
       if (nom.contains("CHIMIE")) return "PCT";
       if (nom.contains("SVT")) return "SVT";
       if (nom.contains("HISTOIRE")) return "HG";
       if (nom.contains("GEOGRAPHIE")) return "HG";
       if (nom.contains("EDUCATION PHYSIQUE")) return "EPS";
       if (nom.contains("SPORT")) return "EPS";
       if (nom.contains("EDUCATION CIVIQUE")) return "ECM";
       if (nom.contains("MORALE")) return "ECM";
       if (nom.contains("MUSIQUE")) return "MUS";
       if (nom.contains("AGRICULTURE")) return "AGRI";
    
       return nom.length() > 3 ? nom.substring(0, 3) : nom;
    }

    /**
     * Formate une cellule pour affichage
     */
    public String formaterCellule(Map<String, String> cellule) {
        if (cellule == null) return "";
        return cellule.getOrDefault("valeur", "");
    }
    
    /**
     * Vérifie si un créneau est une pause
     */
    public boolean isPause(int numeroCreneau) {
        return numeroCreneau == 4 || numeroCreneau == 7;
    }
}