package com.edt.services;

import com.edt.dtos.*;
import com.edt.entities.*;
import com.edt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GenerationService {
    
    @Autowired
    private EnseignementRepository enseignementRepository;
    
    @Autowired
    private EmploiDuTempsRepository emploiDuTempsRepository;
    
    @Autowired
    private CreneauHoraireRepository creneauHoraireRepository;
    
    @Autowired
    private ClasseRepository classeRepository;
    
    @Autowired
    private EnseignantRepository enseignantRepository;
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    // ========== CONSTANTES ==========
    private static final List<String> JOURS_SEMAINE = Arrays.asList("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI");
    private static final List<String> JOURS_SOIR = Arrays.asList("LUNDI", "MARDI", "JEUDI");
    
    // ========== GÉNÉRATION POUR CLASSE ==========
    
    public GenerationResultDTO genererPourClasse(String classeId, String anneeScolaire) {
        return genererPourClasse(classeId, anneeScolaire, new GenerationOptionsDTO());
    }
    
    public GenerationResultDTO genererPourClasse(String classeId, String anneeScolaire, GenerationOptionsDTO options) {
        System.out.println("\n" + "🔥".repeat(60));
        System.out.println("🔥 GÉNÉRATION POUR CLASSE - DÉBUT");
        System.out.println("🔥 Classe ID: " + classeId);
        System.out.println("🔥 Année: " + anneeScolaire);
        System.out.println("🔥".repeat(60));
        
        try {
            // 1. Récupérer la classe
            Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée: " + classeId));
            System.out.println("✅ Classe: " + classe.getNom());
            
            // 2. Récupérer les enseignements
            List<Enseignement> enseignements = enseignementRepository.findByClasseId(classeId);
            System.out.println("✅ Enseignements trouvés: " + enseignements.size());
            
            if (enseignements.isEmpty()) {
                return new GenerationResultDTO(false, "Aucun enseignement pour la classe " + classe.getNom());
            }
            
            // 3. CRÉER L'EMPLOI DU TEMPS
            EmploiDuTemps emploi = new EmploiDuTemps();
            emploi.setNom("Emploi du temps - " + classe.getNom() + " - " + anneeScolaire);
            emploi.setAnneeScolaire(anneeScolaire);
            emploi.setClasse(classe);
            emploi.setDateGeneration(LocalDate.now());
            emploi.setStatut("EN_COURS");
            emploi = emploiDuTempsRepository.save(emploi);
            System.out.println("✅ Emploi créé: " + emploi.getId());
            
            // 4. CRÉER LES CRÉNEAUX
            int totalCreneaux = this.creerCreneauxForces(emploi);
            System.out.println("✅ Créneaux créés: " + totalCreneaux);
            
            // 5. VÉRIFICATION
            List<CreneauHoraire> verification = creneauHoraireRepository.findByEmploiDuTempsId(emploi.getId());
            System.out.println("🔍 VÉRIFICATION: " + verification.size() + " créneaux en base");
            
            if (verification.isEmpty()) {
                throw new RuntimeException("ÉCHEC: Aucun créneau n'a été créé !");
            }
            
            // 6. AFFECTER LES COURS
            int affectations = this.affecterCoursForce(emploi, enseignements, verification);
            System.out.println("✅ Cours affectés: " + affectations);
            
            // 🔴 DIAGNOSTIC
            if (affectations == 0) {
                System.err.println("❌ CRITIQUE: Aucun cours n'a été affecté !");
                System.err.println("   Nombre d'enseignements: " + enseignements.size());
                System.err.println("   Nombre de créneaux: " + verification.size());
                System.err.println("   Créneaux libres: " + verification.stream().filter(c -> c.isEstLibre()).count());
            } else {
                long occupes = verification.stream().filter(c -> !c.isEstLibre()).count();
                System.out.println("   Vérification: " + occupes + " créneaux occupés");
            }
            
            // 7. FINALISER
            emploi.setStatut("TERMINE");
            emploiDuTempsRepository.save(emploi);
            
            GenerationResultDTO result = new GenerationResultDTO(true, 
                "Génération réussie: " + affectations + " cours créés sur " + totalCreneaux + " créneaux",
                emploi.getId());
            
            Map<String, Object> details = new HashMap<>();
            details.put("classe", classe.getNom());
            details.put("total_creneaux", totalCreneaux);
            details.put("cours_affectes", affectations);
            details.put("enseignements", enseignements.size());
            result.setDetails(details);
            
            System.out.println("\n✅ SUCCÈS ! Emploi du temps généré: " + emploi.getId());
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
            return new GenerationResultDTO(false, "Erreur: " + e.getMessage());
        }
    }
    
    // ========== CRÉATION FORCÉE DES CRÉNEAUX ==========
    
    private int creerCreneauxForces(EmploiDuTemps emploi) {
        System.out.println("\n📅 CRÉATION FORCÉE DES CRÉNEAUX");
        int compteur = 0;
        
        // HORAIRES FIXES - MATIN (tous les jours)
        String[][] creneauxMatin = {
            {"07:00", "07:55"},
            {"07:55", "08:50"},
            {"08:50", "09:45"},
            {"09:45", "10:10"}, // PAUSE
            {"10:10", "11:05"},
            {"11:05", "12:00"},
            {"12:00", "15:00"}  // PAUSE MIDI
        };
        
        // HORAIRES FIXES - SOIR (LUNDI, MARDI, JEUDI seulement)
        String[][] creneauxSoir = {
            {"15:00", "15:55"},
            {"15:55", "16:50"},
            {"16:50", "17:45"}
        };
        
        for (String jour : JOURS_SEMAINE) {
            System.out.println("  📌 Traitement de " + jour);
            
            // Créer les créneaux du matin pour TOUS les jours
            for (int i = 0; i < creneauxMatin.length; i++) {
                CreneauHoraire creneau = new CreneauHoraire();
                creneau.setEmploiDuTemps(emploi);
                creneau.setJourSemaine(jour);
                creneau.setHeureDebut(creneauxMatin[i][0]);
                creneau.setHeureFin(creneauxMatin[i][1]);
                creneau.setNumeroCreneau(i + 1);
                creneau.setEstLibre(true);
                creneau.setClasse(null);
                creneau.setEnseignant(null);
                creneau.setMatiere(null);
                creneau.setSalle(null);
                
                creneauHoraireRepository.save(creneau);
                compteur++;
            }
            
            // Créer les créneaux du soir pour LUNDI, MARDI, JEUDI
            if (JOURS_SOIR.contains(jour)) {
                for (int i = 0; i < creneauxSoir.length; i++) {
                    CreneauHoraire creneau = new CreneauHoraire();
                    creneau.setEmploiDuTemps(emploi);
                    creneau.setJourSemaine(jour);
                    creneau.setHeureDebut(creneauxSoir[i][0]);
                    creneau.setHeureFin(creneauxSoir[i][1]);
                    creneau.setNumeroCreneau(creneauxMatin.length + i + 1);
                    creneau.setEstLibre(true);
                    creneau.setClasse(null);
                    creneau.setEnseignant(null);
                    creneau.setMatiere(null);
                    creneau.setSalle(null);
                    
                    creneauHoraireRepository.save(creneau);
                    compteur++;
                }
            }
        }
        
        System.out.println("  ✅ Total créneaux créés: " + compteur);
        return compteur;
    }
    
     // ========== ALGORITHME D'AFFECTATION SANS CHEVAUCHEMENT ==========

    private int affecterCoursForce(EmploiDuTemps emploi, List<Enseignement> enseignements, List<CreneauHoraire> tousCreneaux) {
    System.out.println("\n" + "=".repeat(80));
    System.out.println("📊 AFFECTATION SANS CHEVAUCHEMENT - VERSION FINALE");
    System.out.println("=".repeat(80));
    
    // 1. DÉFINIR LES CRÉNEAUX AUTORISÉS
    Set<Integer> creneauxAutorises = new HashSet<>(Arrays.asList(1, 2, 3, 5, 6, 8, 9, 10));
    
    // 2. MAPS DE CONTRÔLE GLOBAL (TOUTES CLASSES CONFONDUES)
    Map<String, Set<String>> enseignantsOccupesGlobal = new HashMap<>(); // "JOUR_HEURE" -> enseignants (TOUTES CLASSES)
    Map<String, Set<Integer>> matieresPlaceesParJour = new HashMap<>(); // "CLASSE_MATIERE" -> créneaux déjà utilisés
    
    // 3. PRÉPARER LES ENSEIGNEMENTS
    List<EnseignementInfo> enseignementList = new ArrayList<>();
    for (Enseignement e : enseignements) {
        int maxHeures = JOURS_SEMAINE.size() * 5; // 5 créneaux par jour max
        int heuresReelles = Math.min(e.getHeuresParSemaine(), maxHeures);
        
        enseignementList.add(new EnseignementInfo(
            e,
            e.getMatiere().getCode(),
            e.getEnseignant().getNom(),
            heuresReelles
        ));
        System.out.println("  📋 " + e.getMatiere().getCode() + " - " + 
            e.getEnseignant().getNom() + " (" + heuresReelles + "h)");
    }
    
    // Trier par heures (décroissant)
    enseignementList.sort((a, b) -> Integer.compare(b.heures, a.heures));
    
    // 4. ORGANISER LES CRÉNEAUX PAR JOUR
    Map<String, List<Integer>> creneauxParJour = new LinkedHashMap<>();
    
    for (String jour : JOURS_SEMAINE) {
        List<Integer> creneauxJour = new ArrayList<>();
        for (CreneauHoraire c : tousCreneaux) {
            if (c.getJourSemaine().equals(jour) && 
                creneauxAutorises.contains(c.getNumeroCreneau()) &&
                c.isEstLibre()) {
                
                if (c.getNumeroCreneau() >= 8 && !JOURS_SOIR.contains(jour)) continue;
                creneauxJour.add(c.getNumeroCreneau());
            }
        }
        Collections.sort(creneauxJour);
        creneauxParJour.put(jour, creneauxJour);
    }
    
    // Afficher les créneaux disponibles
    System.out.println("\n📅 CRÉNEAUX DISPONIBLES PAR JOUR:");
    for (String jour : JOURS_SEMAINE) {
        System.out.println("   " + jour + ": " + creneauxParJour.get(jour).size() + " créneaux");
    }
    
    // 5. AFFECTATION AVEC CONTRÔLE GLOBAL
    Random random = new Random();
    int totalAffectes = 0;
    
    for (EnseignementInfo ens : enseignementList) {
        int heuresRestantes = ens.heures;
        String matiereId = ens.enseignement.getMatiere().getId();
        String classeId = ens.enseignement.getClasse().getId();
        String matiereKey = classeId + "_" + matiereId;
        String enseignantId = ens.enseignement.getEnseignant().getId();
        
        System.out.println("\n  📋 Placement de " + ens.code + " (" + heuresRestantes + "h) - " + ens.enseignant);
        
        if (heuresRestantes == 0) continue;
        
        boolean estEPS = ens.code != null && ens.code.toUpperCase().contains("EPS");
        
        int heuresPlacees = 0;
        int tentatives = 0;
        int maxTentatives = 500;
        
        while (heuresPlacees < heuresRestantes && tentatives < maxTentatives) {
            tentatives++;
            
            // Mélanger les jours
            List<String> joursMelanges = new ArrayList<>(JOURS_SEMAINE);
            Collections.shuffle(joursMelanges, random);
            
            for (String jour : joursMelanges) {
                if (heuresPlacees >= heuresRestantes) break;
                
                List<Integer> creneauxJour = creneauxParJour.get(jour);
                if (creneauxJour.isEmpty()) continue;
                
                // Filtrer les créneaux valides pour CETTE classe ET cet enseignant
                List<Integer> creneauxValides = new ArrayList<>();
                
                for (int ordre : creneauxJour) {
                    // RÈGLE 1: Pas de même matière deux fois le même jour dans la même classe
                    String cleMatiereJour = matiereKey + "_" + jour;
                    if (matieresPlaceesParJour.containsKey(cleMatiereJour)) {
                        continue; // Cette matière est déjà placée ce jour dans cette classe
                    }
                    
                    // RÈGLE 2: EPS aux bons créneaux
                    if (estEPS && !(ordre <= 3 || ordre >= 8)) continue;
                    
                    // RÈGLE 3: Vérification GLOBALE - enseignant pas occupé dans UNE AUTRE classe
                    String cleOccupation = jour + "_" + ordre;
                    if (enseignantsOccupesGlobal.containsKey(cleOccupation) && 
                        enseignantsOccupesGlobal.get(cleOccupation).contains(enseignantId)) {
                        continue; // Enseignant déjà occupé dans une autre classe à ce créneau
                    }
                    
                    creneauxValides.add(ordre);
                }
                
                if (!creneauxValides.isEmpty()) {
                    // Choisir un créneau
                    int ordreChoisi = creneauxValides.get(random.nextInt(creneauxValides.size()));
                    
                    // Trouver le créneau
                    for (CreneauHoraire creneau : tousCreneaux) {
                        if (creneau.getJourSemaine().equals(jour) && 
                            creneau.getNumeroCreneau() == ordreChoisi) {
                            
                            // Affecter
                            creneau.setEnseignant(ens.enseignement.getEnseignant());
                            creneau.setClasse(ens.enseignement.getClasse());
                            creneau.setMatiere(ens.enseignement.getMatiere());
                            creneau.setEstLibre(false);
                            creneau.setSalle(genererSalle(ens.code));
                            
                            creneauHoraireRepository.save(creneau);
                            
                            // Mettre à jour les suivis GLOBAUX
                            String cleOccupation = jour + "_" + ordreChoisi;
                            enseignantsOccupesGlobal.computeIfAbsent(cleOccupation, k -> new HashSet<>())
                                .add(enseignantId);
                            
                            // Marquer cette matière comme placée ce jour dans cette classe
                            matieresPlaceesParJour.put(matiereKey + "_" + jour, new HashSet<>());
                            
                            // Retirer le créneau des disponibles
                            creneauxParJour.get(jour).remove((Integer) ordreChoisi);
                            
                            heuresPlacees++;
                            totalAffectes++;
                            
                            System.out.println("    ✓ " + jour + " " + formatHoraire(ordreChoisi));
                            break;
                        }
                    }
                }
            }
        }
        
        if (heuresPlacees < heuresRestantes) {
            System.out.println("    ⚠️ Manque " + (heuresRestantes - heuresPlacees) + "h pour " + ens.code);
        } else {
            System.out.println("    ✅ Complet: " + heuresPlacees + "h placées");
        }
    }
    
    // 6. VÉRIFICATION FINALE DES CHEVAUCHEMENTS
    System.out.println("\n" + "=".repeat(80));
    System.out.println("📊 VÉRIFICATION FINALE DES CHEVAUCHEMENTS");
    System.out.println("=".repeat(80));
    
    Map<String, List<String>> verificationChevauchement = new HashMap<>();
    boolean chevauchementTrouve = false;
    
    for (CreneauHoraire c : tousCreneaux) {
        if (!c.isEstLibre()) {
            String jour = c.getJourSemaine();
            String heureKey = jour + "_" + c.getNumeroCreneau();
            String info = c.getClasse().getNom() + " - " + 
                         c.getMatiere().getCode() + " - " + 
                         c.getEnseignant().getNom();
            
            if (verificationChevauchement.containsKey(heureKey)) {
                System.err.println("❌ CHEVAUCHEMENT À " + jour + " " + formatHoraire(c.getNumeroCreneau()) + ":");
                for (String existing : verificationChevauchement.get(heureKey)) {
                    System.err.println("   - " + existing);
                }
                System.err.println("   + " + info);
                chevauchementTrouve = true;
            } else {
                List<String> list = new ArrayList<>();
                list.add(info);
                verificationChevauchement.put(heureKey, list);
            }
        }
    }
    
    if (!chevauchementTrouve) {
        System.out.println("✅ Aucun chevauchement d'enseignant détecté !");
    }
    
    // Afficher le tableau
    afficherTableauEmploiDuTemps(tousCreneaux);
    
    System.out.println("\n  ✅ TOTAL: " + totalAffectes + " cours affectés");
    return totalAffectes;
    }

    // ========== GÉNÉRATION GLOBALE SANS CHEVAUCHEMENT ==========

    public GenerationResultDTO genererGlobalSansChevauchement(String anneeScolaire, GenerationOptionsDTO options) {
    System.out.println("\n" + "🌟".repeat(60));
    System.out.println("🌟 GÉNÉRATION GLOBALE SANS CHEVAUCHEMENT");
    System.out.println("📅 Année: " + anneeScolaire);
    System.out.println("🌟".repeat(60));
    
    try {
        // 1. Récupérer TOUTES les classes
        List<Classe> toutesLesClasses = classeRepository.findAll();
        System.out.println("📚 " + toutesLesClasses.size() + " classes trouvées");
        
        // 2. MAP GLOBALE des occupations (TOUTES classes confondues)
        Map<String, Set<String>> occupationsGlobales = new HashMap<>(); // "JOUR_HEURE" -> enseignants
        
        // 3. Pour CHAQUE classe, créer un emploi du temps
        List<EmploiDuTemps> emploisGeneres = new ArrayList<>();
        int totalCours = 0;
        
        for (Classe classe : toutesLesClasses) {
            System.out.println("\n" + "-".repeat(40));
            System.out.println("🏫 Traitement de la classe: " + classe.getNom());
            
            // Récupérer les enseignements de cette classe
            List<Enseignement> enseignements = enseignementRepository.findByClasseId(classe.getId());
            
            if (enseignements.isEmpty()) {
                System.out.println("   ⚠️ Aucun enseignement, ignorée");
                continue;
            }
            
            // Créer l'emploi du temps pour cette classe
            EmploiDuTemps emploi = new EmploiDuTemps();
            emploi.setNom("Emploi du temps - " + classe.getNom() + " - " + anneeScolaire);
            emploi.setAnneeScolaire(anneeScolaire);
            emploi.setClasse(classe);
            emploi.setDateGeneration(LocalDate.now());
            emploi.setStatut("EN_COURS");
            emploi = emploiDuTempsRepository.save(emploi);
            
            // Créer les créneaux
            creerCreneauxForces(emploi);
            List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploi.getId());
            
            // Affecter les cours de CETTE classe en respectant les occupations globales
            int coursClasse = affecterCoursAvecContraintesGlobales(emploi, enseignements, creneaux, occupationsGlobales);
            totalCours += coursClasse;
            
            emploi.setStatut("TERMINE");
            emploiDuTempsRepository.save(emploi);
            emploisGeneres.add(emploi);
            
            System.out.println("   ✅ Emploi généré: " + emploi.getId() + " (" + coursClasse + " cours)");
        }
        
        // 4. Vérification finale
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 VÉRIFICATION FINALE DES CHEVAUCHEMENTS");
        System.out.println("=".repeat(80));
        
        boolean chevauchementTrouve = false;
        Map<String, List<String>> verification = new HashMap<>();
        
        for (EmploiDuTemps emploi : emploisGeneres) {
            List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploi.getId());
            for (CreneauHoraire c : creneaux) {
                if (!c.isEstLibre()) {
                    String key = c.getJourSemaine() + "_" + c.getNumeroCreneau();
                    String info = c.getClasse().getNom() + " - " + 
                                 c.getMatiere().getCode() + " - " + 
                                 c.getEnseignant().getNom();
                    
                    if (verification.containsKey(key)) {
                        System.err.println("❌ CHEVAUCHEMENT À " + key + " (" + formatHoraire(c.getNumeroCreneau()) + "):");
                        for (String existing : verification.get(key)) {
                            System.err.println("   - " + existing);
                        }
                        System.err.println("   + " + info);
                        chevauchementTrouve = true;
                    } else {
                        List<String> list = new ArrayList<>();
                        list.add(info);
                        verification.put(key, list);
                    }
                }
            }
        }
        
        if (!chevauchementTrouve) {
            System.out.println("✅ AUCUN CHEVAUCHEMENT DÉTECTÉ !");
        }
        
        String message = String.format(
            "Génération terminée: %d emplois, %d cours, %s",
            emploisGeneres.size(), totalCours,
            chevauchementTrouve ? "⚠️ avec chevauchements" : "✅ sans chevauchement"
        );
        
        GenerationResultDTO result = new GenerationResultDTO(true, message);
        
        Map<String, Object> details = new HashMap<>();
        details.put("total_emplois", emploisGeneres.size());
        details.put("total_cours", totalCours);
        details.put("emplois_generes", emploisGeneres.stream().map(EmploiDuTemps::getId).collect(Collectors.toList()));
        details.put("chevauchements", chevauchementTrouve ? "DÉTECTÉS" : "AUCUN");
        result.setDetails(details);
        
        return result;
        
    } catch (Exception e) {
        System.err.println("❌ ERREUR: " + e.getMessage());
        e.printStackTrace();
        return new GenerationResultDTO(false, "Erreur: " + e.getMessage());
    }
  }

// ========== AFFECTATION AVEC CONTRAINTES GLOBALES ==========

    private int affecterCoursAvecContraintesGlobales(
    EmploiDuTemps emploi, 
    List<Enseignement> enseignements, 
    List<CreneauHoraire> tousCreneaux,
    Map<String, Set<String>> occupationsGlobales) {
    
    System.out.println("\n   📍 Affectation avec contraintes globales");
    
    Set<Integer> creneauxAutorises = new HashSet<>(Arrays.asList(1, 2, 3, 5, 6, 8, 9, 10));
    
    // Préparer les enseignements
    List<EnseignementInfo> enseignementList = new ArrayList<>();
    for (Enseignement e : enseignements) {
        enseignementList.add(new EnseignementInfo(
            e, e.getMatiere().getCode(), e.getEnseignant().getNom(), e.getHeuresParSemaine()
        ));
    }
    
    // Trier par heures
    enseignementList.sort((a, b) -> Integer.compare(b.heures, a.heures));
    
    // Organiser les créneaux disponibles
    Map<String, List<Integer>> creneauxParJour = new HashMap<>();
    for (String jour : JOURS_SEMAINE) {
        List<Integer> creneauxJour = new ArrayList<>();
        for (CreneauHoraire c : tousCreneaux) {
            if (c.getJourSemaine().equals(jour) && 
                creneauxAutorises.contains(c.getNumeroCreneau()) &&
                c.isEstLibre()) {
                
                if (c.getNumeroCreneau() >= 8 && !JOURS_SOIR.contains(jour)) continue;
                creneauxJour.add(c.getNumeroCreneau());
            }
        }
        Collections.sort(creneauxJour);
        creneauxParJour.put(jour, creneauxJour);
    }
    
    // Suivi local (pour cette classe)
    Set<String> matieresPlaceesCeJour = new HashSet<>();
    
    Random random = new Random();
    int totalAffectes = 0;
    
    for (EnseignementInfo ens : enseignementList) {
        int heuresRestantes = ens.heures;
        String matiereKey = ens.enseignement.getMatiere().getId();
        String enseignantId = ens.enseignement.getEnseignant().getId();
        boolean estEPS = ens.code != null && ens.code.toUpperCase().contains("EPS");
        
        System.out.println("      " + ens.code + " (" + heuresRestantes + "h)");
        
        if (heuresRestantes == 0) continue;
        
        int heuresPlacees = 0;
        int tentatives = 0;
        
        while (heuresPlacees < heuresRestantes && tentatives < 200) {
            tentatives++;
            
            List<String> joursMelanges = new ArrayList<>(JOURS_SEMAINE);
            Collections.shuffle(joursMelanges, random);
            
            for (String jour : joursMelanges) {
                if (heuresPlacees >= heuresRestantes) break;
                
                String cleMatiereJour = matiereKey + "_" + jour;
                if (matieresPlaceesCeJour.contains(cleMatiereJour)) continue;
                
                List<Integer> creneauxJour = creneauxParJour.get(jour);
                if (creneauxJour.isEmpty()) continue;
                
                // Filtrer les créneaux
                List<Integer> valides = new ArrayList<>();
                for (int ordre : creneauxJour) {
                    // RÈGLE EPS
                    if (estEPS && !(ordre <= 3 || ordre >= 8)) continue;
                    
                    // Vérifier occupation GLOBALE
                    String cleOccupation = jour + "_" + ordre;
                    if (occupationsGlobales.containsKey(cleOccupation) && 
                        occupationsGlobales.get(cleOccupation).contains(enseignantId)) {
                        continue;
                    }
                    
                    valides.add(ordre);
                }
                
                if (!valides.isEmpty()) {
                    int ordreChoisi = valides.get(random.nextInt(valides.size()));
                    
                    // Trouver et affecter le créneau
                    for (CreneauHoraire creneau : tousCreneaux) {
                        if (creneau.getJourSemaine().equals(jour) && 
                            creneau.getNumeroCreneau() == ordreChoisi) {
                            
                            creneau.setEnseignant(ens.enseignement.getEnseignant());
                            creneau.setClasse(ens.enseignement.getClasse());
                            creneau.setMatiere(ens.enseignement.getMatiere());
                            creneau.setEstLibre(false);
                            creneau.setSalle(genererSalle(ens.code));
                            
                            creneauHoraireRepository.save(creneau);
                            
                            // Marquer occupation GLOBALE
                            String cleOccupation = jour + "_" + ordreChoisi;
                            occupationsGlobales.computeIfAbsent(cleOccupation, k -> new HashSet<>())
                                .add(enseignantId);
                            
                            // Marquer pour cette classe
                            matieresPlaceesCeJour.add(matiereKey + "_" + jour);
                            
                            // Retirer des disponibles
                            creneauxParJour.get(jour).remove((Integer) ordreChoisi);
                            
                            heuresPlacees++;
                            totalAffectes++;
                            break;
                        }
                    }
                }
            }
        }
        
        if (heuresPlacees < heuresRestantes) {
            System.out.println("      ⚠️ Manque " + (heuresRestantes - heuresPlacees) + "h pour " + ens.code);
        }
    }
    
    return totalAffectes;
   }
    
    // ========== CLASSES AUXILIAIRES ==========
    
    private class EnseignementInfo {
        Enseignement enseignement;
        String code;
        String enseignant;
        int heures;
        
        EnseignementInfo(Enseignement e, String c, String ens, int h) {
            this.enseignement = e;
            this.code = c;
            this.enseignant = ens;
            this.heures = h;
        }
    }
    
    // ========== MÉTHODES UTILITAIRES ==========
    
    private String formatHoraire(int ordre) {
        switch (ordre) {
            case 1: return "07h00-07h55";
            case 2: return "07h55-08h50";
            case 3: return "08h50-09h45";
            case 5: return "10h10-11h05";
            case 6: return "11h05-12h00";
            case 8: return "15h00-15h55";
            case 9: return "15h55-16h50";
            case 10: return "16h50-17h45";
            default: return "";
        }
    }
    
    private String genererSalle(String codeMatiere) {
        if (codeMatiere == null) return "Salle " + (100 + new Random().nextInt(50));
        
        codeMatiere = codeMatiere.toUpperCase();
        
        if (codeMatiere.contains("EPS")) return "Gymnase";
        if (codeMatiere.contains("MUS")) return "Salle Musique";
        if (codeMatiere.contains("PCT") || codeMatiere.contains("SVT")) return "Labo " + (100 + new Random().nextInt(20));
        if (codeMatiere.contains("INFO")) return "Labo Info";
        if (codeMatiere.contains("AGRI")) return "Atelier Agro";
        return "Salle " + (100 + new Random().nextInt(50));
    }
    
    private void afficherTableauEmploiDuTemps(List<CreneauHoraire> tousCreneaux) {
        System.out.println("\n" + "=".repeat(90));
        System.out.println("📋 EMPLOI DU TEMPS GÉNÉRÉ");
        System.out.println("=".repeat(90));
        
        if (!tousCreneaux.isEmpty() && tousCreneaux.get(0).getClasse() != null) {
            System.out.println("Classe: " + tousCreneaux.get(0).getClasse().getNom());
        }
        System.out.println();
        
        System.out.printf("%-12s", "");
        for (String jour : JOURS_SEMAINE) {
            System.out.printf("%-15s", jour);
        }
        System.out.println();
        System.out.println("-".repeat(90));
        
        int[] ordres = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        String[] horaires = {"07h00-07h55", "07h55-08h50", "08h50-09h45", "09h45-10h10",
                             "10h10-11h05", "11h05-12h00", "12h00-15h00",
                             "15h00-15h55", "15h55-16h50", "16h50-17h45"};
        String[] types = {"COURS", "COURS", "COURS", "RECREATION",
                          "COURS", "COURS", "GRANDE PAUSE",
                          "COURS", "COURS", "COURS"};
        
        for (int i = 0; i < ordres.length; i++) {
            int ordre = ordres[i];
            String horaire = horaires[i];
            String type = types[i];
            
            if (type.equals("RECREATION") || type.equals("GRANDE PAUSE")) {
                System.out.printf("\u001B[33m%-12s\u001B[0m", horaire);
            } else {
                System.out.printf("%-12s", horaire);
            }
            
            for (String jour : JOURS_SEMAINE) {
                if ((ordre == 8 || ordre == 9 || ordre == 10) && !JOURS_SOIR.contains(jour)) {
                    System.out.printf("%-15s", "");
                    continue;
                }
                
                if (type.equals("RECREATION") || type.equals("GRANDE PAUSE")) {
                    System.out.printf("\u001B[33m%-15s\u001B[0m", type);
                    continue;
                }
                
                String contenu = "-";
                for (CreneauHoraire c : tousCreneaux) {
                    if (c.getJourSemaine().equals(jour) && 
                        c.getNumeroCreneau() == ordre && 
                        !c.isEstLibre()) {
                        
                        String matiere = c.getMatiere() != null ? c.getMatiere().getCode() : "";
                        String enseignant = c.getEnseignant() != null ? c.getEnseignant().getNom() : "";
                        contenu = matiere + " " + enseignant;
                        break;
                    }
                }
                System.out.printf("%-15s", contenu);
            }
            System.out.println();
        }
    }
    
    // ========== MÉTHODES POUR LES CONTROLEURS ==========
    
    public Map<String, Object> diagnostiquerBaseDeDonnees() {
        Map<String, Object> diagnostic = new HashMap<>();
        try {
            diagnostic.put("classes", classeRepository.count());
            diagnostic.put("enseignants", enseignantRepository.count());
            diagnostic.put("matieres", matiereRepository.count());
            diagnostic.put("enseignements", enseignementRepository.count());
            diagnostic.put("emplois_du_temps", emploiDuTempsRepository.count());
            diagnostic.put("creneaux_horaires", creneauHoraireRepository.count());
            diagnostic.put("status", "OK");
            diagnostic.put("timestamp", LocalDateTime.now().toString());
        } catch (Exception e) {
            diagnostic.put("status", "ERROR");
            diagnostic.put("message", e.getMessage());
        }
        return diagnostic;
    }
    
    public EmploiDuTempsDTO genererEmploiDuTempsGlobal(String anneeScolaire) {
        EmploiDuTemps emploi = new EmploiDuTemps();
        emploi.setNom("Emploi du temps global - " + anneeScolaire);
        emploi.setAnneeScolaire(anneeScolaire);
        emploi.setDateGeneration(LocalDate.now());
        emploi.setStatut("NON_IMPLEMENTE");
        emploi = emploiDuTempsRepository.save(emploi);
        return convertEmploiToDTO(emploi);
    }
    
    public EmploiDuTempsDTO genererEmploiDuTempsClasse(String classeId, String anneeScolaire) {
        return genererEmploiDuTempsClasse(classeId, anneeScolaire, new GenerationOptionsDTO());
    }
    
    public EmploiDuTempsDTO genererEmploiDuTempsClasse(String classeId, String anneeScolaire, GenerationOptionsDTO options) {
        GenerationResultDTO result = genererPourClasse(classeId, anneeScolaire, options);
        return emploiDuTempsRepository.findById(result.getEmploiDuTempsId())
            .map(this::convertEmploiToDTO)
            .orElse(null);
    }
    
    public EmploiDuTempsDTO genererEmploiDuTempsEnseignant(String enseignantId, String anneeScolaire) {
        return genererEmploiDuTempsEnseignant(enseignantId, anneeScolaire, new GenerationOptionsDTO());
    }
    
    public EmploiDuTempsDTO genererEmploiDuTempsEnseignant(String enseignantId, String anneeScolaire, GenerationOptionsDTO options) {
        EmploiDuTemps emploi = new EmploiDuTemps();
        emploi.setNom("Emploi du temps enseignant - " + anneeScolaire);
        emploi.setAnneeScolaire(anneeScolaire);
        emploi.setDateGeneration(LocalDate.now());
        emploi.setStatut("NON_IMPLEMENTE");
        emploi = emploiDuTempsRepository.save(emploi);
        return convertEmploiToDTO(emploi);
    }
    
    public List<EmploiDuTempsDTO> getAllEmploisDuTemps() {
        return emploiDuTempsRepository.findAll()
            .stream()
            .map(this::convertEmploiToDTO)
            .collect(Collectors.toList());
    }
    
    public List<EmploiDuTempsDTO> getEmploisGlobal() {
        return emploiDuTempsRepository.findAll()
            .stream()
            .filter(e -> e.getClasse() == null)
            .map(this::convertEmploiToDTO)
            .collect(Collectors.toList());
    }
    
    public List<EmploiDuTempsDTO> getEmploisParClasse(String classeId) {
        return emploiDuTempsRepository.findByClasseId(classeId)
            .stream()
            .map(this::convertEmploiToDTO)
            .collect(Collectors.toList());
    }
    
    public EmploiDuTempsDTO getEmploiDuTemps(String id) {
        return emploiDuTempsRepository.findById(id)
            .map(this::convertEmploiToDTO)
            .orElse(null);
    }
    
    public EmploiDuTempsDTO updateStatutEmploi(String id, String statut) {
        return emploiDuTempsRepository.findById(id)
            .map(emploi -> {
                emploi.setStatut(statut);
                return convertEmploiToDTO(emploiDuTempsRepository.save(emploi));
            })
            .orElse(null);
    }
    
    public List<CreneauHoraireDTO> getCreneauxParEmploi(String emploiId) {
        return creneauHoraireRepository.findByEmploiDuTempsId(emploiId)
            .stream()
            .map(this::convertCreneauToDTO)
            .collect(Collectors.toList());
    }
    
    public List<CreneauHoraireDTO> getCreneauxParEnseignant(String enseignantId) {
        return creneauHoraireRepository.findByEnseignantId(enseignantId)
            .stream()
            .map(this::convertCreneauToDTO)
            .collect(Collectors.toList());
    }
    
    public List<CreneauHoraireDTO> getCreneauxParClasse(String classeId) {
        return creneauHoraireRepository.findByClasseId(classeId)
            .stream()
            .map(this::convertCreneauToDTO)
            .collect(Collectors.toList());
    }
    
    public List<CreneauHoraireDTO> getCreneauxParJour(String emploiId, String jour) {
        return creneauHoraireRepository.findByEmploiDuTempsId(emploiId)
            .stream()
            .filter(c -> c.getJourSemaine().equals(jour))
            .map(this::convertCreneauToDTO)
            .collect(Collectors.toList());
    }
    
    public CreneauHoraireDTO updateCreneau(CreneauHoraireDTO dto) {
        return creneauHoraireRepository.findById(dto.getId())
            .map(creneau -> {
                creneau.setSalle(dto.getSalle());
                creneau.setEstLibre(dto.getEstLibre());
                return convertCreneauToDTO(creneauHoraireRepository.save(creneau));
            })
            .orElse(null);
    }
    
    public CreneauHoraireDTO affecterCreneau(AffectationCreneauDTO affectation) {
        return creneauHoraireRepository.findById(affectation.getCreneauId())
            .map(creneau -> {
                creneau.setEnseignant(enseignantRepository.findById(affectation.getEnseignantId()).orElse(null));
                creneau.setClasse(classeRepository.findById(affectation.getClasseId()).orElse(null));
                creneau.setMatiere(matiereRepository.findById(affectation.getMatiereId()).orElse(null));
                creneau.setSalle(affectation.getSalle());
                creneau.setEstLibre(false);
                return convertCreneauToDTO(creneauHoraireRepository.save(creneau));
            })
            .orElse(null);
    }
    
    public CreneauHoraireDTO libererCreneau(String creneauId) {
        return creneauHoraireRepository.findById(creneauId)
            .map(creneau -> {
                creneau.setEnseignant(null);
                creneau.setClasse(null);
                creneau.setMatiere(null);
                creneau.setSalle(null);
                creneau.setEstLibre(true);
                return convertCreneauToDTO(creneauHoraireRepository.save(creneau));
            })
            .orElse(null);
    }
    
    public StatistiquesEmploiDTO getStatistiquesEmploi(String emploiId) {
        StatistiquesEmploiDTO stats = new StatistiquesEmploiDTO();
        stats.setEmploiId(emploiId);
        List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploiId);
        stats.setTotalCreneaux(creneaux.size());
        stats.setCreneauxOccupes((int) creneaux.stream().filter(c -> !c.isEstLibre()).count());
        stats.setCreneauxLibres(creneaux.size() - stats.getCreneauxOccupes());
        return stats;
    }
    
    public StatistiquesEnseignantDTO getStatistiquesEnseignant(String enseignantId) {
        StatistiquesEnseignantDTO stats = new StatistiquesEnseignantDTO();
        stats.setEnseignantId(enseignantId);
        return stats;
    }
    
    public byte[] exporterEmploiDuTemps(String emploiId, String format) {
        return ("Export de l'emploi du temps " + emploiId).getBytes();
    }
    
    public List<GenerationHistoriqueDTO> getHistoriqueGenerations() {
        return emploiDuTempsRepository.findAll()
            .stream()
            .sorted(Comparator.comparing(EmploiDuTemps::getDateGeneration).reversed())
            .map(emploi -> {
                GenerationHistoriqueDTO dto = new GenerationHistoriqueDTO();
                dto.setId(emploi.getId());
                dto.setNom(emploi.getNom());
                dto.setAnneeScolaire(emploi.getAnneeScolaire());
                dto.setDateGeneration(emploi.getDateGeneration());
                dto.setStatut(emploi.getStatut());
                if (emploi.getClasse() != null) {
                    dto.setClasseNom(emploi.getClasse().getNom());
                }
                
                List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploi.getId());
                dto.setTotalCreneaux(creneaux.size());
                dto.setCreneauxOccupes((int) creneaux.stream().filter(c -> !c.isEstLibre()).count());
                
                return dto;
            })
            .collect(Collectors.toList());
    }
    
    public GenerationStatutDTO getStatutGeneration(String emploiId) {
        GenerationStatutDTO statut = new GenerationStatutDTO();
        emploiDuTempsRepository.findById(emploiId).ifPresent(emploi -> {
            statut.setId(emploiId);
            statut.setNom(emploi.getNom());
            statut.setStatut(emploi.getStatut());
            statut.setDateGeneration(emploi.getDateGeneration());
            
            List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploiId);
            statut.setTotalCreneaux(creneaux.size());
            statut.setCreneauxOccupes((int) creneaux.stream().filter(c -> !c.isEstLibre()).count());
            statut.setCreneauxLibres(creneaux.size() - statut.getCreneauxOccupes());
            
            if (creneaux.size() > 0) {
                double taux = (statut.getCreneauxOccupes() * 100.0) / creneaux.size();
                statut.setTauxOccupation(taux);
                statut.setProgression((int) taux);
            }
        });
        return statut;
    }
    
    // ========== MÉTHODE DE SUPPRESSION CORRIGÉE ==========
    
    @Transactional
    public void deleteEmploiDuTemps(String id) {
        System.out.println("\n" + "🗑️".repeat(40));
        System.out.println("🗑️ SUPPRESSION DE L'EMPLOI: " + id);
        System.out.println("🗑️".repeat(40));
        
        try {
            // 1. Vérifier si l'emploi existe
            EmploiDuTemps emploi = emploiDuTempsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé: " + id));
            
            System.out.println("   📋 Emploi trouvé: " + emploi.getNom());
            
            // 2. Récupérer tous les créneaux associés
            List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(id);
            System.out.println("   📅 Créneaux associés: " + creneaux.size());
            
            // 3. SUPPRIMER D'ABORD TOUS LES CRÉNEAUX
            if (!creneaux.isEmpty()) {
                System.out.println("   🗑️ Suppression des créneaux...");
                creneauHoraireRepository.deleteAll(creneaux);
                creneauHoraireRepository.flush(); // Forcer l'exécution immédiate
                System.out.println("   ✅ Créneaux supprimés");
            }
            
            // 4. ENSUITE SUPPRIMER L'EMPLOI DU TEMPS
            System.out.println("   🗑️ Suppression de l'emploi...");
            emploiDuTempsRepository.delete(emploi);
            emploiDuTempsRepository.flush(); // Forcer l'exécution immédiate
            
            System.out.println("   ✅ SUPPRESSION TERMINÉE");
            System.out.println("🗑️".repeat(40) + "\n");
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR LORS DE LA SUPPRESSION: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression: " + e.getMessage(), e);
        }
    }
    
    public List<String> getAnneesScolaires() {
        return emploiDuTempsRepository.findAll()
            .stream()
            .map(EmploiDuTemps::getAnneeScolaire)
            .filter(Objects::nonNull)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .collect(Collectors.toList());
    }
    
    public String getAnneeScolaireCourante() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        if (month >= 9) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }
    
    // ========== GÉNÉRATION GLOBALE CORRIGÉE ==========
    
    public GenerationResultDTO genererGlobal(String anneeScolaire) {
        return genererGlobal(anneeScolaire, new GenerationOptionsDTO());
    }
    
    public GenerationResultDTO genererGlobal(String anneeScolaire, GenerationOptionsDTO options) {
        System.out.println("\n" + "🌍".repeat(60));
        System.out.println("🌍 GÉNÉRATION GLOBALE - TOUTES LES CLASSES");
        System.out.println("📅 Année: " + anneeScolaire);
        System.out.println("🌍".repeat(60));
        
        try {
            List<Classe> toutesLesClasses = classeRepository.findAll();
            
            if (toutesLesClasses.isEmpty()) {
                return new GenerationResultDTO(false, "Aucune classe trouvée dans la base");
            }
            
            System.out.println("📚 " + toutesLesClasses.size() + " classes trouvées");
            
            int totalEmplois = 0;
            int totalCours = 0;
            List<String> emploisGeneres = new ArrayList<>();
            
            for (Classe classe : toutesLesClasses) {
                System.out.println("\n" + "-".repeat(40));
                System.out.println("🏫 Traitement de la classe: " + classe.getNom());
                
                List<Enseignement> enseignements = enseignementRepository.findByClasseId(classe.getId());
                
                if (enseignements.isEmpty()) {
                    System.out.println("   ⚠️ Aucun enseignement pour cette classe, ignorée");
                    continue;
                }
                
                GenerationResultDTO result = genererPourClasse(classe.getId(), anneeScolaire, options);
                
                if (result.isSuccess()) {
                    totalEmplois++;
                    emploisGeneres.add(result.getEmploiDuTempsId());
                    
                    if (result.getDetails() != null) {
                        Map<String, Object> details = (Map<String, Object>) result.getDetails();
                        if (details.containsKey("cours_affectes")) {
                            Object coursObj = details.get("cours_affectes");
                            if (coursObj instanceof Integer) {
                                totalCours += (Integer) coursObj;
                            } else if (coursObj instanceof Long) {
                                totalCours += ((Long) coursObj).intValue();
                            }
                        }
                    }
                    
                    System.out.println("   ✅ Emploi généré: " + result.getEmploiDuTempsId());
                } else {
                    System.out.println("   ❌ Échec: " + result.getMessage());
                }
            }
            
            String message = String.format(
                "Génération globale terminée: %d emplois générés, %d cours au total",
                totalEmplois, totalCours
            );
            
            GenerationResultDTO result = new GenerationResultDTO(true, message);
            
            Map<String, Object> details = new HashMap<>();
            details.put("total_emplois", totalEmplois);
            details.put("total_cours", totalCours);
            details.put("emplois_generes", emploisGeneres);
            result.setDetails(details);
            
            System.out.println("\n" + "✅".repeat(40));
            System.out.println("✅ " + message);
            System.out.println("✅".repeat(40));
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR génération globale: " + e.getMessage());
            e.printStackTrace();
            return new GenerationResultDTO(false, "Erreur: " + e.getMessage());
        }
    }
    
    // ========== GÉNÉRATION POUR ENSEIGNANT CORRIGÉE ==========
    
    public GenerationResultDTO genererPourEnseignant(String enseignantId, String anneeScolaire) {
        return genererPourEnseignant(enseignantId, anneeScolaire, new GenerationOptionsDTO());
    }
    
    public GenerationResultDTO genererPourEnseignant(String enseignantId, String anneeScolaire, GenerationOptionsDTO options) {
        System.out.println("\n" + "👤".repeat(60));
        System.out.println("👤 GÉNÉRATION POUR ENSEIGNANT");
        System.out.println("👤 ID: " + enseignantId);
        System.out.println("📅 Année: " + anneeScolaire);
        System.out.println("👤".repeat(60));
        
        try {
            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé: " + enseignantId));
            
            System.out.println("👤 Enseignant: " + enseignant.getNom() + " " + 
                (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""));
            
            List<Enseignement> enseignements = enseignementRepository.findByEnseignantId(enseignantId);
            
            if (enseignements.isEmpty()) {
                return new GenerationResultDTO(false, 
                    "Aucun enseignement trouvé pour " + enseignant.getNom());
            }
            
            System.out.println("📚 " + enseignements.size() + " enseignements trouvés");
            
            Map<String, List<Enseignement>> enseignementsParClasse = new HashMap<>();
            
            for (Enseignement e : enseignements) {
                String classeId = e.getClasse().getId();
                if (!enseignementsParClasse.containsKey(classeId)) {
                    enseignementsParClasse.put(classeId, new ArrayList<>());
                }
                enseignementsParClasse.get(classeId).add(e);
            }
            
            System.out.println("🏫 Classes concernées: " + enseignementsParClasse.size());
            
            int totalCours = 0;
            List<String> classesTraitees = new ArrayList<>();
            
            for (Map.Entry<String, List<Enseignement>> entry : enseignementsParClasse.entrySet()) {
                String classeId = entry.getKey();
                Classe classe = classeRepository.findById(classeId).orElse(null);
                
                if (classe == null) continue;
                
                System.out.println("\n   🏫 Classe: " + classe.getNom());
                
                List<EmploiDuTemps> emploisExistants = emploiDuTempsRepository
                    .findByClasseId(classeId)
                    .stream()
                    .filter(e -> e.getAnneeScolaire().equals(anneeScolaire))
                    .collect(Collectors.toList());
                
                EmploiDuTemps emploi;
                
                if (!emploisExistants.isEmpty()) {
                    emploi = emploisExistants.get(0);
                    System.out.println("   📅 Emploi existant trouvé: " + emploi.getId());
                } else {
                    emploi = new EmploiDuTemps();
                    emploi.setNom("Emploi du temps - " + classe.getNom() + " - " + anneeScolaire);
                    emploi.setAnneeScolaire(anneeScolaire);
                    emploi.setClasse(classe);
                    emploi.setDateGeneration(LocalDate.now());
                    emploi.setStatut("EN_COURS");
                    emploi = emploiDuTempsRepository.save(emploi);
                    
                    creerCreneauxForces(emploi);
                    System.out.println("   📅 Nouvel emploi créé: " + emploi.getId());
                }
                
                classesTraitees.add(classe.getNom());
                
                List<CreneauHoraire> creneaux = creneauHoraireRepository
                    .findByEmploiDuTempsId(emploi.getId())
                    .stream()
                    .filter(c -> !c.isEstLibre())
                    .filter(c -> c.getEnseignant() != null && 
                                c.getEnseignant().getId().equals(enseignantId))
                    .collect(Collectors.toList());
                
                totalCours += creneaux.size();
                
                System.out.println("   ✅ " + creneaux.size() + " cours trouvés pour cet enseignant");
            }
            
            String message = String.format(
                "Génération pour enseignant terminée: %d cours dans %d classes",
                totalCours, classesTraitees.size()
            );
            
            GenerationResultDTO result = new GenerationResultDTO(true, message);
            
            Map<String, Object> details = new HashMap<>();
            details.put("enseignant", enseignant.getNom() + " " + 
                (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""));
            details.put("total_cours", totalCours);
            details.put("classes", classesTraitees);
            result.setDetails(details);
            
            System.out.println("\n✅ " + message);
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR génération pour enseignant: " + e.getMessage());
            e.printStackTrace();
            return new GenerationResultDTO(false, "Erreur: " + e.getMessage());
        }
    }
    
    // ========== MÉTHODES DE CONVERSION ==========
    
    private EmploiDuTempsDTO convertEmploiToDTO(EmploiDuTemps emploi) {
        EmploiDuTempsDTO dto = new EmploiDuTempsDTO();
        dto.setId(emploi.getId());
        dto.setNom(emploi.getNom());
        dto.setAnneeScolaire(emploi.getAnneeScolaire());
        dto.setDateGeneration(emploi.getDateGeneration());
        dto.setStatut(emploi.getStatut());
        if (emploi.getClasse() != null) {
            dto.setClasseId(emploi.getClasse().getId());
            dto.setClasseNom(emploi.getClasse().getNom());
        }
        return dto;
    }
    
    private CreneauHoraireDTO convertCreneauToDTO(CreneauHoraire creneau) {
        CreneauHoraireDTO dto = new CreneauHoraireDTO();
        dto.setId(creneau.getId());
        dto.setEmploiDuTempsId(creneau.getEmploiDuTemps().getId());
        dto.setJourSemaine(creneau.getJourSemaine());
        dto.setHeureDebut(creneau.getHeureDebut());
        dto.setHeureFin(creneau.getHeureFin());
        dto.setNumeroCreneau(creneau.getNumeroCreneau());
        dto.setEstLibre(creneau.isEstLibre());
        dto.setSalle(creneau.getSalle());
        if (creneau.getClasse() != null) {
            dto.setClasseId(creneau.getClasse().getId());
            dto.setClasseNom(creneau.getClasse().getNom());
        }
        if (creneau.getEnseignant() != null) {
            dto.setEnseignantId(creneau.getEnseignant().getId());
            dto.setEnseignantNom(creneau.getEnseignant().getNom() + " " + 
                (creneau.getEnseignant().getPrenom() != null ? creneau.getEnseignant().getPrenom() : ""));
        }
        if (creneau.getMatiere() != null) {
            dto.setMatiereId(creneau.getMatiere().getId());
            dto.setMatiereNom(creneau.getMatiere().getNom());
        }
        return dto;
    }
}