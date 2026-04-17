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
    
    @Autowired
    private EcoleRepository ecoleRepository;
    
    // ========== CONSTANTES ==========
    private static final List<String> JOURS_SEMAINE = Arrays.asList("LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI");
    private static final List<String> JOURS_SOIR = Arrays.asList("LUNDI", "MARDI", "JEUDI");
    
    // Créneaux autorisés pour les cours (excluant les pauses)
    private static final Set<Integer> CRENEAUX_AUTORISES = new HashSet<>(Arrays.asList(1, 2, 3, 5, 6, 8, 9, 10));
    
    // SUPPRIMEZ cette ligne : private Map<String, Set<String>> occupationsGlobales = new HashMap<>();
    
    // Créneaux de pause
    private static final Set<Integer> CRENEAUX_PAUSE = new HashSet<>(Arrays.asList(4, 7));
    
    private void supprimerEmploisExistants(String classeId, String anneeScolaire, String type) {
        List<EmploiDuTemps> existants = emploiDuTempsRepository
            .findByClasseId(classeId)
            .stream()
            .filter(e -> e.getAnneeScolaire().equals(anneeScolaire))
            .collect(Collectors.toList());
        
        for (EmploiDuTemps e : existants) {
            // NE SUPPRIMER QUE SI LE TYPE CORRESPOND
            // Utiliser getNom() comme fallback si getType() est null
            String typeEmploi = e.getType();
            if (typeEmploi == null) {
                // Fallback: déduire le type du nom
                if (e.getNom() != null && e.getNom().toLowerCase().contains("classe")) {
                    typeEmploi = "CLASSE";
                } else if (e.getNom() != null && e.getNom().toLowerCase().contains("enseignant")) {
                    typeEmploi = "ENSEIGNANT";
                } else {
                    typeEmploi = "GLOBAL";
                }
            }
            
            if (type.equals(typeEmploi)) {
                System.out.println("🗑️ Suppression de l'emploi " + type + ": " + e.getId());
                List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(e.getId());
                if (!creneaux.isEmpty()) {
                    creneauHoraireRepository.deleteAll(creneaux);
                }
                emploiDuTempsRepository.delete(e);
            }
        }
        creneauHoraireRepository.flush();
        emploiDuTempsRepository.flush();
    }
    
    public GenerationResultDTO genererPourClasse(String classeId, String anneeScolaire, GenerationOptionsDTO options) {
        System.out.println("\n" + "🔥".repeat(60));
        System.out.println("🔥 GÉNÉRATION POUR CLASSE - DÉBUT");
        System.out.println("🔥 Classe ID: " + classeId);
        System.out.println("🔥 Année: " + anneeScolaire);
        System.out.println("🔥".repeat(60));
        
        try {
            Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée: " + classeId));
            System.out.println("✅ Classe: " + classe.getNom());
            
            // Supprimer les anciens emplois de type "CLASSE"
            supprimerEmploisExistants(classeId, anneeScolaire, "CLASSE");
            
            List<Enseignement> enseignements = enseignementRepository.findByClasseId(classeId);
            System.out.println("✅ Enseignements trouvés: " + enseignements.size());
            
            if (enseignements.isEmpty()) {
                return new GenerationResultDTO(false, "Aucun enseignement pour la classe " + classe.getNom());
            }
            
            EmploiDuTemps emploi = new EmploiDuTemps();
            emploi.setNom("Emploi du temps - " + classe.getNom() + " - " + anneeScolaire);
            emploi.setAnneeScolaire(anneeScolaire);
            emploi.setClasse(classe);
            emploi.setDateGeneration(LocalDate.now());
            emploi.setStatut("TERMINE");
            emploi.setType("CLASSE");  // ✅ AJOUT DU TYPE
            emploi = emploiDuTempsRepository.save(emploi);
            System.out.println("✅ Emploi créé: " + emploi.getId() + " (Type: CLASSE)");
            
            int totalCreneaux = this.creerCreneauxForces(emploi);
            System.out.println("✅ Créneaux créés: " + totalCreneaux);
            
            List<CreneauHoraire> verification = creneauHoraireRepository.findByEmploiDuTempsId(emploi.getId());
            System.out.println("🔍 VÉRIFICATION: " + verification.size() + " créneaux en base");
            
            if (verification.isEmpty()) {
                throw new RuntimeException("ÉCHEC: Aucun créneau n'a été créé !");
            }
            
            // CRÉER UNE MAP LOCALE pour cette génération
            Map<String, Set<String>> occupationsLocales = new HashMap<>();
            
            int affectations = this.affecterCoursAvecContraintesGlobalesPourClasse(
                emploi, enseignements, verification, occupationsLocales);
            System.out.println("✅ Cours affectés: " + affectations);
            
            if (affectations == 0) {
                System.err.println("❌ CRITIQUE: Aucun cours n'a été affecté !");
            } else {
                long occupes = verification.stream().filter(c -> !c.isEstLibre()).count();
                System.out.println("   Vérification: " + occupes + " créneaux occupés");
                afficherTableauEmploiDuTemps(verification);
            }
            
            emploi.setStatut("TERMINE");
            emploiDuTempsRepository.save(emploi);
            
            GenerationResultDTO result = new GenerationResultDTO(true, 
                "Génération réussie: " + affectations + " cours créés sur " + totalCreneaux + " créneaux",
                emploi.getId());
            
            Map<String, Object> details = new HashMap<>();
            details.put("classe", classe.getNom());
            details.put("type", "CLASSE");  // ✅ AJOUT DU TYPE DANS LES DÉTAILS
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

    private int affecterCoursAvecContraintesGlobalesPourClasse(
        EmploiDuTemps emploi, 
        List<Enseignement> enseignements, 
        List<CreneauHoraire> tousCreneaux,
        Map<String, Set<String>> occupationsLocales) {
        
        System.out.println("\n   📍 Affectation pour classe avec contraintes globales");
        
        List<EnseignementInfo> enseignementList = new ArrayList<>();
        for (Enseignement e : enseignements) {
            enseignementList.add(new EnseignementInfo(
                e, e.getMatiere().getCode(), e.getEnseignant().getNom(), e.getHeuresParSemaine()
            ));
        }
        
        enseignementList.sort((a, b) -> Integer.compare(b.heures, a.heures));
        
        Map<String, List<Integer>> creneauxParJour = new HashMap<>();
        Map<String, List<Integer>> creneauxMatinParJour = new HashMap<>();
        Map<String, List<Integer>> creneauxSoirParJour = new HashMap<>();
        
        for (String jour : JOURS_SEMAINE) {
            List<Integer> creneauxJour = new ArrayList<>();
            List<Integer> creneauxMatin = new ArrayList<>();
            List<Integer> creneauxSoir = new ArrayList<>();
            
            for (CreneauHoraire c : tousCreneaux) {
                if (c.getJourSemaine().equals(jour) && 
                    CRENEAUX_AUTORISES.contains(c.getNumeroCreneau()) &&
                    c.isEstLibre()) {
                    
                    int ordre = c.getNumeroCreneau();
                    if (ordre >= 8) {
                        if (JOURS_SOIR.contains(jour)) {
                            creneauxSoir.add(ordre);
                            creneauxJour.add(ordre);
                        }
                    } else {
                        creneauxMatin.add(ordre);
                        creneauxJour.add(ordre);
                    }
                }
            }
            
            Collections.sort(creneauxJour);
            Collections.sort(creneauxMatin);
            Collections.sort(creneauxSoir);
            
            creneauxParJour.put(jour, creneauxJour);
            creneauxMatinParJour.put(jour, creneauxMatin);
            creneauxSoirParJour.put(jour, creneauxSoir);
        }
        
        Random random = new Random();
        int totalAffectes = 0;
        
        // NOUVELLE MAP : Pour suivre les matières déjà placées par jour et par période
        Map<String, Set<String>> matieresParJourMatin = new HashMap<>();
        Map<String, Set<String>> matieresParJourSoir = new HashMap<>();
        
        for (EnseignementInfo ens : enseignementList) {
            int heuresRestantes = ens.heures;
            String enseignantId = ens.enseignement.getEnseignant().getId();
            String matiereId = ens.enseignement.getMatiere().getId();
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
                    
                    // *** NOUVELLE LOGIQUE ***
                    // Essayer de placer le matin ou le soir selon les besoins
                    boolean possibleMatin = !creneauxMatinParJour.get(jour).isEmpty();
                    boolean possibleSoir = JOURS_SOIR.contains(jour) && !creneauxSoirParJour.get(jour).isEmpty();
                    
                    // Vérifier si on peut encore placer le matin (pas de cours de cette matière le matin)
                    boolean matinDispo = possibleMatin;
                    if (matieresParJourMatin.containsKey(jour) && 
                        matieresParJourMatin.get(jour).contains(matiereId)) {
                        matinDispo = false; // Cette matière a déjà un cours le matin
                    }
                    
                    // Vérifier si on peut encore placer le soir (pas de cours de cette matière le soir)
                    boolean soirDispo = possibleSoir;
                    if (matieresParJourSoir.containsKey(jour) && 
                        matieresParJourSoir.get(jour).contains(matiereId)) {
                        soirDispo = false; // Cette matière a déjà un cours le soir
                    }
                    
                    // Si aucune période disponible, passer au jour suivant
                    if (!matinDispo && !soirDispo) continue;
                    
                    // Décider aléatoirement de mettre le cours le matin ou le soir
                    boolean mettreMatin = false;
                    if (matinDispo && soirDispo) {
                        mettreMatin = random.nextBoolean();
                    } else if (matinDispo) {
                        mettreMatin = true;
                    } else if (soirDispo) {
                        mettreMatin = false;
                    } else {
                        continue;
                    }
                    
                    if (mettreMatin) {
                        // Placer le matin
                        List<Integer> creneauxMatin = creneauxMatinParJour.get(jour);
                        List<Integer> valides = new ArrayList<>();
                        
                        for (int ordre : creneauxMatin) {
                            if (estEPS && !(ordre <= 3)) continue;
                            
                            String cleOccupation = jour + "_" + ordre;
                            if (occupationsLocales.containsKey(cleOccupation) && 
                                occupationsLocales.get(cleOccupation).contains(enseignantId)) {
                                continue;
                            }
                            
                            valides.add(ordre);
                        }
                        
                        if (!valides.isEmpty()) {
                            int ordreChoisi = valides.get(random.nextInt(valides.size()));
                            
                            for (CreneauHoraire creneau : tousCreneaux) {
                                if (creneau.getJourSemaine().equals(jour) && 
                                    creneau.getNumeroCreneau() == ordreChoisi) {
                                    
                                    creneau.setEnseignant(ens.enseignement.getEnseignant());
                                    creneau.setClasse(ens.enseignement.getClasse());
                                    creneau.setMatiere(ens.enseignement.getMatiere());
                                    creneau.setEstLibre(false);
                                    creneau.setSalle(genererSalle(ens.code));
                                    
                                    creneauHoraireRepository.save(creneau);
                                    
                                    String cleOccupation = jour + "_" + ordreChoisi;
                                    occupationsLocales.computeIfAbsent(cleOccupation, k -> new HashSet<>())
                                        .add(enseignantId);
                                    
                                    // Marquer cette matière comme placée le matin
                                    matieresParJourMatin.computeIfAbsent(jour, k -> new HashSet<>())
                                        .add(matiereId);
                                    
                                    creneauxMatinParJour.get(jour).remove((Integer) ordreChoisi);
                                    creneauxParJour.get(jour).remove((Integer) ordreChoisi);
                                    
                                    heuresPlacees++;
                                    totalAffectes++;
                                    break;
                                }
                            }
                        }
                    } else {
                        // Placer le soir
                        List<Integer> creneauxSoir = creneauxSoirParJour.get(jour);
                        List<Integer> valides = new ArrayList<>();
                        
                        for (int ordre : creneauxSoir) {
                            if (estEPS && !(ordre >= 8)) continue;
                            
                            String cleOccupation = jour + "_" + ordre;
                            if (occupationsLocales.containsKey(cleOccupation) && 
                                occupationsLocales.get(cleOccupation).contains(enseignantId)) {
                                continue;
                            }
                            
                            valides.add(ordre);
                        }
                        
                        if (!valides.isEmpty()) {
                            int ordreChoisi = valides.get(random.nextInt(valides.size()));
                            
                            for (CreneauHoraire creneau : tousCreneaux) {
                                if (creneau.getJourSemaine().equals(jour) && 
                                    creneau.getNumeroCreneau() == ordreChoisi) {
                                    
                                    creneau.setEnseignant(ens.enseignement.getEnseignant());
                                    creneau.setClasse(ens.enseignement.getClasse());
                                    creneau.setMatiere(ens.enseignement.getMatiere());
                                    creneau.setEstLibre(false);
                                    creneau.setSalle(genererSalle(ens.code));
                                    
                                    creneauHoraireRepository.save(creneau);
                                    
                                    String cleOccupation = jour + "_" + ordreChoisi;
                                    occupationsLocales.computeIfAbsent(cleOccupation, k -> new HashSet<>())
                                        .add(enseignantId);
                                    
                                    // Marquer cette matière comme placée le soir
                                    matieresParJourSoir.computeIfAbsent(jour, k -> new HashSet<>())
                                        .add(matiereId);
                                    
                                    creneauxSoirParJour.get(jour).remove((Integer) ordreChoisi);
                                    creneauxParJour.get(jour).remove((Integer) ordreChoisi);
                                    
                                    heuresPlacees++;
                                    totalAffectes++;
                                    break;
                                }
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
    
    public GenerationResultDTO genererPourEnseignant(String enseignantId, String anneeScolaire, GenerationOptionsDTO options) {
        System.out.println("\n" + "👤".repeat(60));
        System.out.println("👤 GÉNÉRATION POUR ENSEIGNANT - SYNCHRONISÉE AVEC LES CLASSES");
        System.out.println("👤 ID: " + enseignantId);
        System.out.println("📅 Année: " + anneeScolaire);
        System.out.println("👤".repeat(60));
        
        try {
            Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé: " + enseignantId));
            
            System.out.println("👤 Enseignant: " + enseignant.getNom() + " " + 
                (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""));
            
            // Récupérer tous les enseignements de cet enseignant
            List<Enseignement> enseignements = enseignementRepository.findByEnseignantId(enseignantId);
            
            if (enseignements.isEmpty()) {
                return new GenerationResultDTO(false, 
                    "Aucun enseignement trouvé pour " + enseignant.getNom());
            }
            
            System.out.println("📚 " + enseignements.size() + " enseignements trouvés");
            
            // ===== DIAGNOSTIC DES HEURES PAR MATIÈRE =====
            System.out.println("\n📊 HEURES PAR MATIÈRE (paramétrées):");
            Map<String, Integer> heuresParametrees = new HashMap<>();
            for (Enseignement e : enseignements) {
                String key = e.getMatiere().getCode() + " - " + e.getClasse().getNom();
                heuresParametrees.put(key, e.getHeuresParSemaine());
                System.out.println("   " + key + ": " + e.getHeuresParSemaine() + "h");
            }
            
            // Récupérer toutes les classes de cet enseignant
            Set<String> classesIds = enseignements.stream()
                .map(e -> e.getClasse().getId())
                .collect(Collectors.toSet());
            
            System.out.println("🏫 Classes concernées: " + classesIds.size());
            
            // ===== VÉRIFICATION PRÉALABLE : EXISTENCE DES EMPLOIS DE CLASSE OU GLOBAL =====
            List<String> classesSansEmploi = new ArrayList<>();
            Map<String, EmploiDuTemps> emploisClasseParClasse = new HashMap<>();
            
            for (String classeId : classesIds) {
                Classe classe = classeRepository.findById(classeId).orElse(null);
                if (classe == null) continue;
                
                // Chercher l'emploi du temps pour cette classe et année (type CLASSE ou GLOBAL)
                List<EmploiDuTemps> emploisTrouves = emploiDuTempsRepository.findByClasseId(classeId)
                    .stream()
                    .filter(e -> e.getAnneeScolaire().equals(anneeScolaire))
                    .filter(e -> "CLASSE".equals(e.getType()) || "GLOBAL".equals(e.getType())) // ✅ Accepte les deux types
                    .collect(Collectors.toList());
                
                if (emploisTrouves.isEmpty()) {
                    classesSansEmploi.add(classe.getNom());
                    System.out.println("   ⚠️ Classe sans emploi: " + classe.getNom());
                } else {
                    // Prendre le plus récent (ou celui de type CLASSE en priorité)
                    EmploiDuTemps emploiChoisi = emploisTrouves.stream()
                        .filter(e -> "CLASSE".equals(e.getType()))
                        .findFirst()
                        .orElse(emploisTrouves.get(0)); // Sinon prendre le premier (GLOBAL)
                    
                    emploisClasseParClasse.put(classeId, emploiChoisi);
                    System.out.println("   ✅ Emploi trouvé pour " + classe.getNom() + 
                        " (type: " + emploiChoisi.getType() + ")");
                }
            }
            
            // Si des classes n'ont pas d'emploi du temps, bloquer la génération
            if (!classesSansEmploi.isEmpty()) {
                String messageErreur = String.format(
                    "❌ GÉNÉRATION IMPOSSIBLE\n\n" +
                    "Aucun emploi du temps (CLASSE ou GLOBAL) trouvé pour l'année scolaire %s\n" +
                    "pour les classes suivantes : %s\n\n" +
                    "Veuillez d'abord générer les emplois du temps :\n" +
                    "- Soit en générant classe par classe\n" +
                    "- Soit en utilisant la génération globale\n\n" +
                    "Une fois les emplois du temps des classes créés, vous pourrez générer\n" +
                    "les emplois du temps individuels des enseignants.",
                    anneeScolaire,
                    String.join(", ", classesSansEmploi)
                );
                
                System.err.println("\n" + messageErreur);
                
                GenerationResultDTO result = new GenerationResultDTO(false, messageErreur);
                Map<String, Object> details = new HashMap<>();
                details.put("enseignant", enseignant.getNom() + " " + 
                    (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""));
                details.put("type", "ENSEIGNANT");
                details.put("classes_sans_emploi", classesSansEmploi);
                details.put("erreur", "Emplois de classe ou global manquants");
                result.setDetails(details);
                
                return result;
            }
            
            System.out.println("\n✅ Toutes les classes ont des emplois du temps. Génération possible.");
            
            List<String> emploisUtilises = new ArrayList<>();
            Map<String, Integer> heuresTrouvees = new HashMap<>();
            
            // Pour chaque classe, extraire les cours de l'enseignant depuis l'emploi de classe
            for (String classeId : classesIds) {
                Classe classe = classeRepository.findById(classeId)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée: " + classeId));
                
                System.out.println("\n   🏫 Traitement de la classe: " + classe.getNom());
                
                EmploiDuTemps emploiClasse = emploisClasseParClasse.get(classeId);
                System.out.println("      ✅ Emploi du temps source: " + emploiClasse.getId() + 
                    " (type: " + emploiClasse.getType() + ")");
                
                // Récupérer tous les créneaux de cet emploi
                List<CreneauHoraire> creneauxClasse = creneauHoraireRepository.findByEmploiDuTempsId(emploiClasse.getId());
                
                // Filtrer pour ne garder que ceux de cet enseignant
                List<CreneauHoraire> creneauxEnseignant = creneauxClasse.stream()
                    .filter(c -> !c.isEstLibre())
                    .filter(c -> c.getEnseignant() != null && c.getEnseignant().getId().equals(enseignantId))
                    .collect(Collectors.toList());
                
                System.out.println("      📅 Créneaux trouvés pour cet enseignant: " + creneauxEnseignant.size());
                
                // Compter les heures par matière pour cette classe
                for (CreneauHoraire c : creneauxEnseignant) {
                    String key = c.getMatiere().getCode() + " - " + classe.getNom();
                    heuresTrouvees.put(key, heuresTrouvees.getOrDefault(key, 0) + 1);
                    System.out.println("         " + c.getJourSemaine() + " " + 
                        c.getHeureDebut() + "-" + c.getHeureFin() + " | " +
                        c.getMatiere().getCode());
                }
                
                // Vérifier si un emploi enseignant existe déjà
                List<EmploiDuTemps> emploisExistant = emploiDuTempsRepository.findByClasseId(classeId)
                    .stream()
                    .filter(e -> e.getAnneeScolaire().equals(anneeScolaire))
                    .filter(e -> "ENSEIGNANT".equals(e.getType()))
                    .filter(e -> e.getEnseignant() != null && e.getEnseignant().getId().equals(enseignantId))
                    .collect(Collectors.toList());
                
                EmploiDuTemps emploiEnseignant;
                
                if (!emploisExistant.isEmpty()) {
                    // Mettre à jour l'existant
                    emploiEnseignant = emploisExistant.get(0);
                    System.out.println("      🔄 Mise à jour de l'emploi enseignant existant: " + emploiEnseignant.getId());
                    
                    // Supprimer les anciens créneaux
                    List<CreneauHoraire> anciensCreneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploiEnseignant.getId());
                    if (!anciensCreneaux.isEmpty()) {
                        creneauHoraireRepository.deleteAll(anciensCreneaux);
                        creneauHoraireRepository.flush();
                    }
                } else {
                    // Créer un nouvel emploi
                    emploiEnseignant = new EmploiDuTemps();
                    emploiEnseignant.setNom("Emploi du temps - " + enseignant.getNom() + " - " + classe.getNom() + " - " + anneeScolaire);
                    emploiEnseignant.setAnneeScolaire(anneeScolaire);
                    emploiEnseignant.setClasse(classe);
                    emploiEnseignant.setEnseignant(enseignant);
                    emploiEnseignant.setDateGeneration(LocalDate.now());
                    emploiEnseignant.setStatut("TERMINE");
                    emploiEnseignant.setType("ENSEIGNANT");
                    emploiEnseignant = emploiDuTempsRepository.save(emploiEnseignant);
                    System.out.println("      📅 Nouvel emploi enseignant créé: " + emploiEnseignant.getId());
                }
                
                // Copier les créneaux de l'enseignant depuis l'emploi de classe
                if (!creneauxEnseignant.isEmpty()) {
                    for (CreneauHoraire c : creneauxEnseignant) {
                        CreneauHoraire nouveauCreneau = new CreneauHoraire();
                        nouveauCreneau.setEmploiDuTemps(emploiEnseignant);
                        nouveauCreneau.setJourSemaine(c.getJourSemaine());
                        nouveauCreneau.setHeureDebut(c.getHeureDebut());
                        nouveauCreneau.setHeureFin(c.getHeureFin());
                        nouveauCreneau.setNumeroCreneau(c.getNumeroCreneau());
                        nouveauCreneau.setEstLibre(c.isEstLibre());
                        nouveauCreneau.setClasse(c.getClasse());
                        nouveauCreneau.setEnseignant(c.getEnseignant());
                        nouveauCreneau.setMatiere(c.getMatiere());
                        nouveauCreneau.setSalle(c.getSalle());
                        
                        creneauHoraireRepository.save(nouveauCreneau);
                    }
                    System.out.println("      ✅ " + creneauxEnseignant.size() + " créneaux copiés vers l'emploi enseignant");
                } else {
                    // Créer des créneaux vides
                    creerCreneauxForces(emploiEnseignant);
                    System.out.println("      ℹ️ Créneaux vides créés (aucun cours pour cet enseignant dans cette classe)");
                }
                
                emploisUtilises.add(emploiEnseignant.getId());
            }
            
            // ===== VÉRIFICATION FINALE =====
            System.out.println("\n📊 VÉRIFICATION FINALE - COMPARAISON HEURES PARAMÉTRÉES VS HEURES TROUVÉES:");
            
            boolean toutEstOK = true;
            int totalParametre = 0;
            int totalTrouve = 0;
            
            for (Map.Entry<String, Integer> entry : heuresParametrees.entrySet()) {
                int trouve = heuresTrouvees.getOrDefault(entry.getKey(), 0);
                String matiere = entry.getKey();
                int parametre = entry.getValue();
                
                System.out.println("   " + matiere + 
                    " : " + trouve + "/" + parametre + "h" +
                    (trouve == parametre ? " ✅" : " ❌"));
                
                if (trouve != parametre) {
                    toutEstOK = false;
                }
                totalParametre += parametre;
                totalTrouve += trouve;
            }
            
            System.out.println("   TOTAL: " + totalTrouve + "/" + totalParametre + "h" +
                (totalTrouve == totalParametre ? " ✅" : " ❌"));
            
            String message;
            if (totalTrouve == 0) {
                message = "⚠️ Génération terminée : AUCUN COURS TROUVÉ pour cet enseignant dans les emplois du temps de classe.";
            } else if (totalTrouve == totalParametre) {
                message = String.format(
                    "✅ Génération réussie : %d/%d cours trouvés dans les emplois de classe, synchronisés avec %d classes",
                    totalTrouve, totalParametre, classesIds.size()
                );
            } else {
                message = String.format(
                    "⚠️ Génération partielle : %d/%d cours trouvés dans les emplois de classe, %d classes traitées",
                    totalTrouve, totalParametre, classesIds.size()
                );
            }
            
            GenerationResultDTO result = new GenerationResultDTO(totalTrouve > 0, message);
            
            Map<String, Object> details = new HashMap<>();
            details.put("enseignant", enseignant.getNom() + " " + 
                (enseignant.getPrenom() != null ? enseignant.getPrenom() : ""));
            details.put("type", "ENSEIGNANT");
            details.put("total_cours_trouves", totalTrouve);
            details.put("total_cours_demandes", totalParametre);
            details.put("classes_traitees", classesIds.size());
            details.put("emplois_utilises", emploisUtilises);
            details.put("synchronise_avec_classes", true);
            details.put("detail_par_matiere", heuresTrouvees);
            result.setDetails(details);
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println(message);
            System.out.println("=".repeat(60));
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ ERREUR génération pour enseignant: " + e.getMessage());
            e.printStackTrace();
            return new GenerationResultDTO(false, "Erreur: " + e.getMessage());
        }
    }
    
    private int affecterCoursPourEnseignantAvecContraintes(
        EmploiDuTemps emploi, 
        List<Enseignement> enseignements, 
        List<CreneauHoraire> tousCreneaux,
        Map<String, Set<String>> occupationsLocales,
        String enseignantId,
        Map<String, Integer> heuresDejaPlacees) {  // Paramètre supplémentaire
        
        System.out.println("\n   📍 Affectation pour enseignant avec contraintes globales");
        
        // ===== DIAGNOSTIC =====
        System.out.println("   📊 Enseignements à placer dans cette classe:");
        for (Enseignement e : enseignements) {
            String key = e.getMatiere().getCode() + " - " + e.getClasse().getNom();
            System.out.println("      " + key + ": " + e.getHeuresParSemaine() + "h");
        }
        
        if (enseignements.isEmpty()) {
            System.out.println("      ✅ Aucun cours à placer dans cette classe");
            return 0;
        }
        
        // Préparer la liste des enseignements avec leurs heures
        List<EnseignementInfo> enseignementList = new ArrayList<>();
        for (Enseignement e : enseignements) {
            enseignementList.add(new EnseignementInfo(
                e, 
                e.getMatiere().getCode(), 
                e.getEnseignant().getNom(), 
                e.getHeuresParSemaine()
            ));
        }
        
        // Trier par heures restantes (décroissant)
        enseignementList.sort((a, b) -> Integer.compare(b.heures, a.heures));
        
        Map<String, List<Integer>> creneauxParJour = new HashMap<>();
        for (String jour : JOURS_SEMAINE) {
            List<Integer> creneauxJour = new ArrayList<>();
            for (CreneauHoraire c : tousCreneaux) {
                if (c.getJourSemaine().equals(jour) && 
                    CRENEAUX_AUTORISES.contains(c.getNumeroCreneau()) &&
                    c.isEstLibre()) {
                    
                    if (c.getNumeroCreneau() >= 8 && !JOURS_SOIR.contains(jour)) continue;
                    creneauxJour.add(c.getNumeroCreneau());
                }
            }
            Collections.sort(creneauxJour);
            creneauxParJour.put(jour, creneauxJour);
        }
        
        Random random = new Random();
        int totalAffectes = 0;
        
        for (EnseignementInfo ens : enseignementList) {
            int heuresRestantes = ens.heures;
            String matiereCode = ens.code;
            String classeNom = ens.enseignement.getClasse().getNom();
            String key = matiereCode + "_" + classeNom;
            boolean estEPS = ens.code != null && ens.code.toUpperCase().contains("EPS");
            
            System.out.println("      📍 Placement de " + matiereCode + " - " + classeNom + " (" + heuresRestantes + "h)");
            
            int heuresPlacees = 0;
            int tentatives = 0;
            
            while (heuresPlacees < heuresRestantes && tentatives < 200) {
                tentatives++;
                
                List<String> joursMelanges = new ArrayList<>(JOURS_SEMAINE);
                Collections.shuffle(joursMelanges, random);
                
                for (String jour : joursMelanges) {
                    if (heuresPlacees >= heuresRestantes) break;
                    
                    List<Integer> creneauxJour = creneauxParJour.get(jour);
                    if (creneauxJour.isEmpty()) continue;
                    
                    List<Integer> valides = new ArrayList<>();
                    for (int ordre : creneauxJour) {
                        if (estEPS && !(ordre <= 3 || ordre >= 8)) continue;
                        
                        // Vérifier les occupations locales (pour cet enseignant uniquement)
                        String cleOccupation = jour + "_" + ordre;
                        if (occupationsLocales.containsKey(cleOccupation) && 
                            occupationsLocales.get(cleOccupation).contains(enseignantId)) {
                            continue;
                        }
                        
                        valides.add(ordre);
                    }
                    
                    if (!valides.isEmpty()) {
                        int ordreChoisi = valides.get(random.nextInt(valides.size()));
                        
                        for (CreneauHoraire creneau : tousCreneaux) {
                            if (creneau.getJourSemaine().equals(jour) && 
                                creneau.getNumeroCreneau() == ordreChoisi &&
                                creneau.isEstLibre()) {
                                
                                creneau.setEnseignant(ens.enseignement.getEnseignant());
                                creneau.setClasse(ens.enseignement.getClasse());
                                creneau.setMatiere(ens.enseignement.getMatiere());
                                creneau.setEstLibre(false);
                                creneau.setSalle(genererSalle(ens.code));
                                
                                creneauHoraireRepository.save(creneau);
                                
                                // Mettre à jour les occupations locales
                                String cleOccupation = jour + "_" + ordreChoisi;
                                occupationsLocales.computeIfAbsent(cleOccupation, k -> new HashSet<>())
                                    .add(enseignantId);
                                
                                creneauxParJour.get(jour).remove((Integer) ordreChoisi);
                                
                                heuresPlacees++;
                                totalAffectes++;
                                
                                // Mettre à jour les heures déjà placées globalement
                                heuresDejaPlacees.put(key, heuresDejaPlacees.getOrDefault(key, 0) + 1);
                                
                                break;
                            }
                        }
                    }
                }
            }
            
            if (heuresPlacees < heuresRestantes) {
                System.out.println("      ⚠️ Manque " + (heuresRestantes - heuresPlacees) + 
                                "h pour " + matiereCode + " - " + classeNom);
            } else {
                System.out.println("      ✅ Complet: " + heuresPlacees + "h placées");
            }
        }
        
        return totalAffectes;
    }
    
    // ========== ALGORITHME SPÉCIFIQUE POUR ENSEIGNANT ==========
    
    private int affecterCoursForcePourEnseignant(
        EmploiDuTemps emploi, 
        List<Enseignement> enseignements, 
        List<CreneauHoraire> creneauxLibres,
        String enseignantId
    ) {
        Random random = new Random();
        int totalAffectes = 0;
        
        Map<String, List<Integer>> creneauxParJour = new HashMap<>();
        for (String jour : JOURS_SEMAINE) {
            List<Integer> creneauxJour = new ArrayList<>();
            for (CreneauHoraire c : creneauxLibres) {
                if (c.getJourSemaine().equals(jour) && 
                    CRENEAUX_AUTORISES.contains(c.getNumeroCreneau())) {
                    creneauxJour.add(c.getNumeroCreneau());
                }
            }
            Collections.sort(creneauxJour);
            creneauxParJour.put(jour, creneauxJour);
        }
        
        for (Enseignement ens : enseignements) {
            int heuresRestantes = ens.getHeuresParSemaine();
            boolean estEPS = ens.getMatiere().getCode() != null && 
                            ens.getMatiere().getCode().toUpperCase().contains("EPS");
            
            int heuresPlacees = 0;
            int tentatives = 0;
            int maxTentatives = 500;
            
            while (heuresPlacees < heuresRestantes && tentatives < maxTentatives) {
                tentatives++;
                
                List<String> joursMelanges = new ArrayList<>(JOURS_SEMAINE);
                Collections.shuffle(joursMelanges, random);
                
                for (String jour : joursMelanges) {
                    if (heuresPlacees >= heuresRestantes) break;
                    
                    List<Integer> creneauxJour = creneauxParJour.get(jour);
                    if (creneauxJour == null || creneauxJour.isEmpty()) continue;
                    
                    List<Integer> valides = new ArrayList<>();
                    for (int ordre : creneauxJour) {
                        if (estEPS && !(ordre <= 3 || ordre >= 8)) continue;
                        valides.add(ordre);
                    }
                    
                    if (!valides.isEmpty()) {
                        int ordreChoisi = valides.get(random.nextInt(valides.size()));
                        
                        for (CreneauHoraire creneau : creneauxLibres) {
                            if (creneau.getJourSemaine().equals(jour) && 
                                creneau.getNumeroCreneau() == ordreChoisi) {
                                
                                creneau.setEnseignant(ens.getEnseignant());
                                creneau.setClasse(ens.getClasse());
                                creneau.setMatiere(ens.getMatiere());
                                creneau.setEstLibre(false);
                                creneau.setSalle(genererSalle(ens.getMatiere().getCode()));
                                
                                creneauHoraireRepository.save(creneau);
                                
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
                System.out.println("      ⚠️ Manque " + (heuresRestantes - heuresPlacees) + 
                                  "h pour " + ens.getMatiere().getCode());
            }
        }
        
        return totalAffectes;
    }
    
    // ========== CRÉATION FORCÉE DES CRÉNEAUX ==========
    
    private int creerCreneauxForces(EmploiDuTemps emploi) {
        System.out.println("\n📅 CRÉATION FORCÉE DES CRÉNEAUX");
        int compteur = 0;
        
        String[][] creneauxMatin = {
            {"07:00", "07:55"},  // 1
            {"07:55", "08:50"},  // 2
            {"08:50", "09:45"},  // 3
            {"09:45", "10:10"},  // 4 (Récréation)
            {"10:10", "11:05"},  // 5
            {"11:05", "12:00"},  // 6
            {"12:00", "15:00"}   // 7 (Grande pause)
        };
        
        String[][] creneauxSoir = {
            {"15:00", "15:55"},  // 8
            {"15:55", "16:50"},  // 9
            {"16:50", "17:45"}   // 10
        };
        
        for (String jour : JOURS_SEMAINE) {
            System.out.println("  📌 Traitement de " + jour);
            
            // Créneaux du matin (toujours présents)
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
            
            // Créneaux du soir (uniquement pour certains jours)
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
    
    // ========== MÉTHODE D'AFFECTATION AMÉLIORÉE AVEC RÉPARTITION ==========

    private int affecterCoursAmeliore(EmploiDuTemps emploi, List<Enseignement> enseignements, List<CreneauHoraire> tousCreneaux) {
       System.out.println("\n" + "=".repeat(80));
       System.out.println("📊 AFFECTATION AMÉLIORÉE - VERSION AVEC RÉPARTITION");
       System.out.println("=".repeat(80));

        // Calculer le nombre total d'heures à placer
       int totalHeuresDemandees = enseignements.stream().mapToInt(Enseignement::getHeuresParSemaine).sum();
       System.out.println("📊 TOTAL D'HEURES À PLACER: " + totalHeuresDemandees);
    
       // Compter les créneaux disponibles
       long creneauxDisponibles = tousCreneaux.stream()
           .filter(c -> CRENEAUX_AUTORISES.contains(c.getNumeroCreneau()))
           .filter(CreneauHoraire::isEstLibre)
           .count();
       System.out.println("📊 CRÉNEAUX DISPONIBLES: " + creneauxDisponibles);

       // Préparer les enseignements avec priorité
       List<EnseignementInfo> enseignementList = new ArrayList<>();
       for (Enseignement e : enseignements) {
          enseignementList.add(new EnseignementInfo(
              e,
              e.getMatiere().getCode(),
              e.getEnseignant().getNom(),
              e.getHeuresParSemaine()
           ));
           System.out.println("  📋 " + e.getMatiere().getCode() + " - " + 
              e.getEnseignant().getNom() + " (" + e.getHeuresParSemaine() + "h)");
        }

       // IMPORTANT: Mélanger l'ordre pour éviter les répétitions systématiques
       Collections.shuffle(enseignementList, new Random());

       // Organiser les créneaux disponibles par jour et par créneau
       Map<String, List<Integer>> creneauxParJour = new HashMap<>();
       Map<String, List<Integer>> creneauxMatinParJour = new HashMap<>();
       Map<String, List<Integer>> creneauxSoirParJour = new HashMap<>();
    
       // Pour suivre la fréquence d'utilisation de chaque créneau
       Map<Integer, Integer> frequenceCreneaux = new HashMap<>();
       for (int i = 1; i <= 10; i++) {
          if (CRENEAUX_AUTORISES.contains(i)) {
              frequenceCreneaux.put(i, 0);
          }
       }

       for (String jour : JOURS_SEMAINE) {
          List<Integer> creneauxJour = new ArrayList<>();
          List<Integer> creneauxMatin = new ArrayList<>();
          List<Integer> creneauxSoir = new ArrayList<>();

          for (CreneauHoraire c : tousCreneaux) {
              if (c.getJourSemaine().equals(jour) && 
                  CRENEAUX_AUTORISES.contains(c.getNumeroCreneau()) &&
                  c.isEstLibre()) {
                
                  int ordre = c.getNumeroCreneau();
                  if (ordre >= 8) {
                      if (JOURS_SOIR.contains(jour)) {
                          creneauxSoir.add(ordre);
                          creneauxJour.add(ordre);
                      }
                   } else {
                      creneauxMatin.add(ordre);
                      creneauxJour.add(ordre);
                  }
              }
           }

           Collections.sort(creneauxJour);
           Collections.sort(creneauxMatin);
           Collections.sort(creneauxSoir);

           creneauxParJour.put(jour, creneauxJour);
           creneauxMatinParJour.put(jour, creneauxMatin);
           creneauxSoirParJour.put(jour, creneauxSoir);
        
           System.out.println("   " + jour + " - Matin: " + creneauxMatin.size() + 
                          ", Soir: " + creneauxSoir.size() + 
                          " (Total: " + creneauxJour.size() + ")");
       }

       // Maps pour éviter les conflits
       Map<String, Set<String>> enseignantsOccupes = new HashMap<>();
       Map<String, Set<String>> coursParMatiereJour = new HashMap<>();
       Map<String, Set<Integer>> creneauxOccupesParClasseJour = new HashMap<>();
    
       // Pour suivre quels créneaux sont déjà très utilisés
       Map<String, Integer> utilisationParCreneau = new HashMap<>();
    
       Random random = new Random();
       int totalAffectes = 0;

       // Important: Pour chaque matière, on va essayer de varier les horaires
       for (EnseignementInfo ens : enseignementList) {
          int heuresRestantes = ens.heures;
          String matiereId = ens.enseignement.getMatiere().getId();
          String classeId = ens.enseignement.getClasse().getId();
          String enseignantId = ens.enseignement.getEnseignant().getId();
          String matiereKey = classeId + "_" + matiereId;
          boolean estEPS = ens.code != null && ens.code.toUpperCase().contains("EPS");
        
          System.out.println("\n  📋 Placement de " + ens.code + " (" + heuresRestantes + "h) - " + ens.enseignant);
        
          // Pour cette matière, on va garder trace des créneaux déjà utilisés
          Set<String> creneauxUtilisesCetteMatiere = new HashSet<>();
        
          int heuresPlacees = 0;
          int tentatives = 0;
          int maxTentatives = 2000;
        
          while (heuresPlacees < heuresRestantes && tentatives < maxTentatives) {
              tentatives++;
            
              // Mélanger les jours pour plus d'équité
              List<String> joursMelanges = new ArrayList<>(JOURS_SEMAINE);
              Collections.shuffle(joursMelanges, random);
            
              for (String jour : joursMelanges) {
                  if (heuresPlacees >= heuresRestantes) break;
                
                  // Vérifier les périodes disponibles pour cette matière
                  String clePeriodeMatin = matiereKey + "_" + jour + "_MATIN";
                  String clePeriodeSoir = matiereKey + "_" + jour + "_SOIR";
                
                  boolean dejaMatin = coursParMatiereJour.containsKey(clePeriodeMatin);
                  boolean dejaSoir = coursParMatiereJour.containsKey(clePeriodeSoir);
                
                  // Maximum 2 cours par jour pour la même matière
                  if (dejaMatin && dejaSoir) continue;
                
                  // Déterminer les périodes possibles
                  boolean possibleMatin = !dejaMatin && !creneauxMatinParJour.get(jour).isEmpty();
                  boolean possibleSoir = !dejaSoir && JOURS_SOIR.contains(jour) && !creneauxSoirParJour.get(jour).isEmpty();
                
                  if (!possibleMatin && !possibleSoir) continue;
                
                  // Récupérer les créneaux déjà occupés dans cette classe ce jour
                  String cleClasseJour = classeId + "_" + jour;
                  Set<Integer> creneauxOccupes = creneauxOccupesParClasseJour.getOrDefault(cleClasseJour, new HashSet<>());
                
                  // Pour les matières non-EPS, on va essayer de varier les créneaux
                  List<Integer> candidatsMatin = new ArrayList<>();
                  List<Integer> candidatsSoir = new ArrayList<>();
                
                 if (estEPS) {
                      // EPS seulement créneaux 1-3 ou 8-10
                      if (possibleMatin) {
                          for (int ordre : creneauxMatinParJour.get(jour)) {
                              if (ordre <= 3 && verifierPlacementPossible(creneauxOccupes, ordre, jour)) {
                                  candidatsMatin.add(ordre);
                              }
                          }
                      }
                      if (possibleSoir) {
                          for (int ordre : creneauxSoirParJour.get(jour)) {
                              if (verifierPlacementPossible(creneauxOccupes, ordre, jour)) {
                                  candidatsSoir.add(ordre);
                              }
                          }
                      }
                  } else {
                      // Pour les autres matières, on va privilégier les créneaux les moins utilisés
                      if (possibleMatin) {
                          // Trier les créneaux par fréquence d'utilisation (les moins utilisés d'abord)
                          List<Integer> creneauxMatinDispo = new ArrayList<>(creneauxMatinParJour.get(jour));
                          creneauxMatinDispo.sort((a, b) -> {
                              int freqA = frequenceCreneaux.getOrDefault(a, 0);
                              int freqB = frequenceCreneaux.getOrDefault(b, 0);
                              return Integer.compare(freqA, freqB);
                          });
                        
                          for (int ordre : creneauxMatinDispo) {
                              if (verifierPlacementPossible(creneauxOccupes, ordre, jour)) {
                                  candidatsMatin.add(ordre);
                              }
                          }
                      }
                    
                      if (possibleSoir) {
                          List<Integer> creneauxSoirDispo = new ArrayList<>(creneauxSoirParJour.get(jour));
                          creneauxSoirDispo.sort((a, b) -> {
                              int freqA = frequenceCreneaux.getOrDefault(a, 0);
                              int freqB = frequenceCreneaux.getOrDefault(b, 0);
                              return Integer.compare(freqA, freqB);
                          });
                        
                          for (int ordre : creneauxSoirDispo) {
                              if (verifierPlacementPossible(creneauxOccupes, ordre, jour)) {
                                  candidatsSoir.add(ordre);
                              }
                          }
                      }
                  }
                
                  // Essayer d'abord le matin puis le soir, ou vice-versa selon le hasard
                  boolean essayerMatinEnPremier = random.nextBoolean();
                
                  if (essayerMatinEnPremier) {
                      if (!candidatsMatin.isEmpty()) {
                          int ordreChoisi = candidatsMatin.get(0); // Prendre le moins utilisé
                          if (affecterCreneau(ens, jour, ordreChoisi, tousCreneaux, 
                                           creneauxParJour, creneauxMatinParJour, creneauxSoirParJour,
                                           enseignantsOccupes, coursParMatiereJour, 
                                           creneauxOccupesParClasseJour, matiereKey, classeId)) {
                              // Incrémenter la fréquence de ce créneau
                              frequenceCreneaux.put(ordreChoisi, frequenceCreneaux.getOrDefault(ordreChoisi, 0) + 1);
                              heuresPlacees++;
                              totalAffectes++;
                              continue;
                          }
                      }
                      if (!candidatsSoir.isEmpty()) {
                          int ordreChoisi = candidatsSoir.get(0);
                          if (affecterCreneau(ens, jour, ordreChoisi, tousCreneaux,
                                           creneauxParJour, creneauxMatinParJour, creneauxSoirParJour,
                                           enseignantsOccupes, coursParMatiereJour,
                                           creneauxOccupesParClasseJour, matiereKey, classeId)) {
                              frequenceCreneaux.put(ordreChoisi, frequenceCreneaux.getOrDefault(ordreChoisi, 0) + 1);
                              heuresPlacees++;
                              totalAffectes++;
                              continue;
                          }
                      }
                  } else {
                      if (!candidatsSoir.isEmpty()) {
                          int ordreChoisi = candidatsSoir.get(0);
                          if (affecterCreneau(ens, jour, ordreChoisi, tousCreneaux,
                                           creneauxParJour, creneauxMatinParJour, creneauxSoirParJour,
                                           enseignantsOccupes, coursParMatiereJour,
                                           creneauxOccupesParClasseJour, matiereKey, classeId)) {
                              frequenceCreneaux.put(ordreChoisi, frequenceCreneaux.getOrDefault(ordreChoisi, 0) + 1);
                              heuresPlacees++;
                              totalAffectes++;
                              continue;
                          }
                      }
                      if (!candidatsMatin.isEmpty()) {
                          int ordreChoisi = candidatsMatin.get(0);
                          if (affecterCreneau(ens, jour, ordreChoisi, tousCreneaux,
                                           creneauxParJour, creneauxMatinParJour, creneauxSoirParJour,
                                           enseignantsOccupes, coursParMatiereJour,
                                           creneauxOccupesParClasseJour, matiereKey, classeId)) {
                              frequenceCreneaux.put(ordreChoisi, frequenceCreneaux.getOrDefault(ordreChoisi, 0) + 1);
                              heuresPlacees++;
                              totalAffectes++;
                              continue;
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

      // Statistiques finales
      long heuresPlaceesTotal = tousCreneaux.stream().filter(c -> !c.isEstLibre()).count();
      System.out.println("\n📊 BILAN FINAL:");
      System.out.println("   - Heures demandées: " + totalHeuresDemandees);
      System.out.println("   - Heures placées: " + totalAffectes);
      System.out.println("   - Créneaux occupés: " + heuresPlaceesTotal + "/" + tousCreneaux.size());
      System.out.println("   - Taux de remplissage: " + (heuresPlaceesTotal * 100 / tousCreneaux.size()) + "%");
    
      // Afficher la répartition par créneau
      System.out.println("\n📊 RÉPARTITION PAR CRÉNEAU:");
      for (int ordre = 1; ordre <= 10; ordre++) {
          if (CRENEAUX_AUTORISES.contains(ordre)) {
              System.out.println("   Créneau " + ordre + " (" + formatHoraire(ordre) + "): " + 
                              frequenceCreneaux.getOrDefault(ordre, 0) + " cours");
          }
      }

       System.out.println("\n  ✅ TOTAL: " + totalAffectes + " cours affectés");
       return totalAffectes;
    }
    
    // ========== MÉTHODES DE PLACEMENT ==========
    
    /**
     * Vérifie si le placement est possible (contrainte de trous minimale)
     */
    private boolean verifierPlacementPossible(Set<Integer> creneauxOccupes, int ordreChoisi, String jour) {
        List<Integer> ordresMatin = Arrays.asList(1, 2, 3, 5, 6);
        List<Integer> ordresSoir = Arrays.asList(8, 9, 10);
        
        // Vérifier uniquement le créneau précédent pour éviter les trous
        if (ordreChoisi <= 6) {
            int index = ordresMatin.indexOf(ordreChoisi);
            if (index > 0) {
                int ordrePrecedent = ordresMatin.get(index - 1);
                // Si le précédent est libre (et ce n'est pas une pause), on ne peut pas placer
                if (!creneauxOccupes.contains(ordrePrecedent) && !estPause(ordrePrecedent)) {
                    return false;
                }
            }
        } else if (ordreChoisi >= 8) {
            int index = ordresSoir.indexOf(ordreChoisi);
            if (index > 0) {
                int ordrePrecedent = ordresSoir.get(index - 1);
                if (!creneauxOccupes.contains(ordrePrecedent) && !estPause(ordrePrecedent)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Affecte un créneau avec toutes les contraintes
     */
    private boolean affecterCreneau(EnseignementInfo ens, String jour, int ordreChoisi,
                                   List<CreneauHoraire> tousCreneaux,
                                   Map<String, List<Integer>> creneauxParJour,
                                   Map<String, List<Integer>> creneauxMatinParJour,
                                   Map<String, List<Integer>> creneauxSoirParJour,
                                   Map<String, Set<String>> enseignantsOccupes,
                                   Map<String, Set<String>> coursParMatiereJour,
                                   Map<String, Set<Integer>> creneauxOccupesParClasseJour,
                                   String matiereKey, String classeId) {
        
        for (CreneauHoraire creneau : tousCreneaux) {
            if (creneau.getJourSemaine().equals(jour) && 
                creneau.getNumeroCreneau() == ordreChoisi && 
                creneau.isEstLibre()) {
                
                // Vérifier que l'enseignant n'est pas déjà occupé à ce moment
                String cleOccupation = jour + "_" + ordreChoisi;
                if (enseignantsOccupes.containsKey(cleOccupation) && 
                    enseignantsOccupes.get(cleOccupation).contains(ens.enseignement.getEnseignant().getId())) {
                    return false;
                }
                
                // Affecter le créneau
                creneau.setEnseignant(ens.enseignement.getEnseignant());
                creneau.setClasse(ens.enseignement.getClasse());
                creneau.setMatiere(ens.enseignement.getMatiere());
                creneau.setEstLibre(false);
                creneau.setSalle(genererSalle(ens.code));
                
                creneauHoraireRepository.save(creneau);
                
                // Mettre à jour toutes les maps de contrôle
                enseignantsOccupes.computeIfAbsent(cleOccupation, k -> new HashSet<>())
                    .add(ens.enseignement.getEnseignant().getId());
                
                String periode = (ordreChoisi <= 6) ? "MATIN" : "SOIR";
                coursParMatiereJour.put(matiereKey + "_" + jour + "_" + periode, new HashSet<>());
                
                String cleClasseJour = classeId + "_" + jour;
                creneauxOccupesParClasseJour.computeIfAbsent(cleClasseJour, k -> new HashSet<>()).add(ordreChoisi);
                
                // Retirer des créneaux disponibles
                creneauxParJour.get(jour).remove((Integer) ordreChoisi);
                if (ordreChoisi <= 6) {
                    creneauxMatinParJour.get(jour).remove((Integer) ordreChoisi);
                } else {
                    creneauxSoirParJour.get(jour).remove((Integer) ordreChoisi);
                }
                
                return true;
            }
        }
        return false;
    }
    
    /**
     * Vérifie si un créneau est une pause
     */
    private boolean estPause(int ordre) {
        return CRENEAUX_PAUSE.contains(ordre);
    }
    
    public GenerationResultDTO genererGlobalSansChevauchement(String anneeScolaire, GenerationOptionsDTO options) {
        System.out.println("\n" + "🌟".repeat(60));
        System.out.println("🌟 GÉNÉRATION GLOBALE SANS CHEVAUCHEMENT");
        System.out.println("📅 Année: " + anneeScolaire);
        System.out.println("🌟".repeat(60));
        
        try {
            List<Classe> toutesLesClasses = classeRepository.findAll();
            System.out.println("📚 " + toutesLesClasses.size() + " classes trouvées");
            
            // Supprimer tous les anciens emplois de type "GLOBAL"
            for (Classe classe : toutesLesClasses) {
                supprimerEmploisExistants(classe.getId(), anneeScolaire, "GLOBAL");
            }
            
            // Map locale pour cette génération
            Map<String, Set<String>> occupationsGlobales = new HashMap<>();
            
            List<EmploiDuTemps> emploisGeneres = new ArrayList<>();
            int totalCours = 0;
            
            // Mélanger les classes pour éviter les biais
            List<Classe> classesMelangees = new ArrayList<>(toutesLesClasses);
            Collections.shuffle(classesMelangees, new Random());
            
            for (Classe classe : classesMelangees) {
                System.out.println("\n" + "-".repeat(40));
                System.out.println("🏫 Traitement de la classe: " + classe.getNom());
                
                List<Enseignement> enseignements = enseignementRepository.findByClasseId(classe.getId());
                
                if (enseignements.isEmpty()) {
                    System.out.println("   ⚠️ Aucun enseignement, ignorée");
                    continue;
                }
                
                EmploiDuTemps emploi = new EmploiDuTemps();
                emploi.setNom("Emploi du temps - " + classe.getNom() + " - " + anneeScolaire);
                emploi.setAnneeScolaire(anneeScolaire);
                emploi.setClasse(classe);
                emploi.setDateGeneration(LocalDate.now());
                emploi.setStatut("TERMINE");
                emploi.setType("GLOBAL");  // ✅ AJOUT DU TYPE
                emploi = emploiDuTempsRepository.save(emploi);
                System.out.println("   ✅ Emploi créé: " + emploi.getId() + " (Type: GLOBAL)");
                
                creerCreneauxForces(emploi);
                List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(emploi.getId());
                
                // Utiliser la MÊME méthode que dans genererPourClasse
                int coursClasse = this.affecterCoursAvecContraintesGlobalesPourClasse(
                    emploi, enseignements, creneaux, occupationsGlobales);
                
                totalCours += coursClasse;
                
                emploi.setStatut("TERMINE");
                emploiDuTempsRepository.save(emploi);
                emploisGeneres.add(emploi);
                
                System.out.println("   ✅ Emploi généré: " + emploi.getId() + " (" + coursClasse + " cours)");
            }
            
            String message = String.format(
                "Génération terminée: %d emplois, %d cours",
                emploisGeneres.size(), totalCours
            );
            
            GenerationResultDTO result = new GenerationResultDTO(true, message);
            
            Map<String, Object> details = new HashMap<>();
            details.put("type", "GLOBAL");  // ✅ AJOUT DU TYPE DANS LES DÉTAILS
            details.put("total_emplois", emploisGeneres.size());
            details.put("total_cours", totalCours);
            details.put("emplois_generes", emploisGeneres.stream().map(EmploiDuTemps::getId).collect(Collectors.toList()));
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
        
        List<EnseignementInfo> enseignementList = new ArrayList<>();
        for (Enseignement e : enseignements) {
            enseignementList.add(new EnseignementInfo(
                e, e.getMatiere().getCode(), e.getEnseignant().getNom(), e.getHeuresParSemaine()
            ));
        }
        
        enseignementList.sort((a, b) -> Integer.compare(b.heures, a.heures));
        
        Map<String, List<Integer>> creneauxParJour = new HashMap<>();
        for (String jour : JOURS_SEMAINE) {
            List<Integer> creneauxJour = new ArrayList<>();
            for (CreneauHoraire c : tousCreneaux) {
                if (c.getJourSemaine().equals(jour) && 
                    CRENEAUX_AUTORISES.contains(c.getNumeroCreneau()) &&
                    c.isEstLibre()) {
                    
                    if (c.getNumeroCreneau() >= 8 && !JOURS_SOIR.contains(jour)) continue;
                    creneauxJour.add(c.getNumeroCreneau());
                }
            }
            Collections.sort(creneauxJour);
            creneauxParJour.put(jour, creneauxJour);
        }
        
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
                    
                    List<Integer> valides = new ArrayList<>();
                    for (int ordre : creneauxJour) {
                        if (estEPS && !(ordre <= 3 || ordre >= 8)) continue;
                        
                        String cleOccupation = jour + "_" + ordre;
                        if (occupationsGlobales.containsKey(cleOccupation) && 
                            occupationsGlobales.get(cleOccupation).contains(enseignantId)) {
                            continue;
                        }
                        
                        valides.add(ordre);
                    }
                    
                    if (!valides.isEmpty()) {
                        int ordreChoisi = valides.get(random.nextInt(valides.size()));
                        
                        for (CreneauHoraire creneau : tousCreneaux) {
                            if (creneau.getJourSemaine().equals(jour) && 
                                creneau.getNumeroCreneau() == ordreChoisi) {
                                
                                creneau.setEnseignant(ens.enseignement.getEnseignant());
                                creneau.setClasse(ens.enseignement.getClasse());
                                creneau.setMatiere(ens.enseignement.getMatiere());
                                creneau.setEstLibre(false);
                                creneau.setSalle(genererSalle(ens.code));
                                
                                creneauHoraireRepository.save(creneau);
                                
                                String cleOccupation = jour + "_" + ordreChoisi;
                                occupationsGlobales.computeIfAbsent(cleOccupation, k -> new HashSet<>())
                                    .add(enseignantId);
                                
                                matieresPlaceesCeJour.add(matiereKey + "_" + jour);
                                
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
                dto.setType(emploi.getType());
                if (emploi.getClasse() != null) {
                    dto.setClasseNom(emploi.getClasse().getNom());
                }
                // ✅ AJOUTER L'ID DE L'ENSEIGNANT
                if (emploi.getEnseignant() != null) {
                    dto.setEnseignantId(emploi.getEnseignant().getId());
                    dto.setEnseignantNom(emploi.getEnseignant().getNom() + " " + 
                        (emploi.getEnseignant().getPrenom() != null ? emploi.getEnseignant().getPrenom() : ""));
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
    
    @Transactional
    public void deleteEmploiDuTemps(String id) {
        System.out.println("\n" + "🗑️".repeat(40));
        System.out.println("🗑️ SUPPRESSION DE L'EMPLOI: " + id);
        System.out.println("🗑️".repeat(40));
        
        try {
            EmploiDuTemps emploi = emploiDuTempsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emploi du temps non trouvé: " + id));
            
            System.out.println("   📋 Emploi trouvé: " + emploi.getNom());
            
            List<CreneauHoraire> creneaux = creneauHoraireRepository.findByEmploiDuTempsId(id);
            System.out.println("   📅 Créneaux associés: " + creneaux.size());
            
            if (!creneaux.isEmpty()) {
                System.out.println("   🗑️ Suppression des créneaux...");
                creneauHoraireRepository.deleteAll(creneaux);
                creneauHoraireRepository.flush();
                System.out.println("   ✅ Créneaux supprimés");
            }
            
            System.out.println("   🗑️ Suppression de l'emploi...");
            emploiDuTempsRepository.delete(emploi);
            emploiDuTempsRepository.flush();
            
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
        if (emploi.getEnseignant() != null) {
            dto.setEnseignantId(emploi.getEnseignant().getId());
            dto.setEnseignantNom(emploi.getEnseignant().getNom() + " " + 
                (emploi.getEnseignant().getPrenom() != null ? emploi.getEnseignant().getPrenom() : ""));
        }
        
        // ✅ Sécuriser l'accès à ecole
        Optional<Ecole> ecoleOpt = ecoleRepository.findFirstByOrderByCreatedAtAsc();
        if (ecoleOpt.isPresent()) {
            Ecole ecole = ecoleOpt.get();
            dto.setEcoleNom(ecole.getNom());
            dto.setEcoleTelephone(ecole.getTelephone());
            dto.setEcoleAdresse(ecole.getAdresse());
            dto.setEcoleLogo(ecole.getLogo());
            dto.setEcoleDevise(ecole.getDevise());
            dto.setEcoleDre(ecole.getDre());
            dto.setEcoleIesg(ecole.getIesg());
            dto.setEcoleBp(ecole.getBp());
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