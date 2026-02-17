package com.edt.services;

import com.edt.dtos.*;
import com.edt.entities.*;
import com.edt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConfigurationService {
    
    @Autowired
    private HoraireJournalierRepository horaireJournalierRepository;
    
    @Autowired
    private EnseignementRepository enseignementRepository;
    
    @Autowired
    private ClasseRepository classeRepository;
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    @Autowired
    private EnseignantRepository enseignantRepository;
    
    // === MÉTHODES POUR LES HORAIRES JOURNALIERS ===
    
    public List<HoraireJournalierDTO> getHorairesJournaliers() {
        try {
            return horaireJournalierRepository.findAll()
                .stream()
                .map(this::convertHoraireToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public HoraireJournalierDTO getHoraireParJour(String jour) {
        try {
            return horaireJournalierRepository.findByJourSemaine(jour.toUpperCase())
                .map(this::convertHoraireToDTO)
                .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public HoraireJournalierDTO sauvegarderHoraire(HoraireJournalierDTO dto) {
        try {
            HoraireJournalier horaire = convertDTOToHoraire(dto);
            genererCreneaux(horaire);
            HoraireJournalier saved = horaireJournalierRepository.save(horaire);
            return convertHoraireToDTO(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la sauvegarde de l'horaire: " + e.getMessage());
        }
    }
    
    public HoraireJournalierDTO updateHoraire(HoraireJournalierDTO dto) {
        try {
            HoraireJournalier horaire = horaireJournalierRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Horaire non trouvé avec l'id: " + dto.getId()));
            
            horaire.setJourSemaine(dto.getJourSemaine());
            horaire.setHeureDebutMatin(dto.getHeureDebutMatin());
            horaire.setHeureFinMatin(dto.getHeureFinMatin());
            horaire.setHeureDebutApresMidi(dto.getHeureDebutApresMidi());
            horaire.setHeureFinApresMidi(dto.getHeureFinApresMidi());
            horaire.setDureeCreneauMinutes(dto.getDureeCreneauMinutes());
            horaire.setEstJourCours(dto.getEstJourCours());
            
            genererCreneaux(horaire);
            HoraireJournalier saved = horaireJournalierRepository.save(horaire);
            return convertHoraireToDTO(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de l'horaire: " + e.getMessage());
        }
    }
    
    public void deleteHoraire(String id) {
        try {
            horaireJournalierRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de l'horaire: " + e.getMessage());
        }
    }
    
    public void initialiserHorairesParDefaut() {
        try {
            initialiserConfigurationParDefaut();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'initialisation des horaires: " + e.getMessage());
        }
    }
    
    // === MÉTHODES POUR LES ENSEIGNEMENTS ===
    
    public EnseignementDTO attribuerEnseignement(EnseignementDTO dto) {
        try {
            // Validation des données
            Classe classe = classeRepository.findById(dto.getClasseId())
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + dto.getClasseId()));
            Matiere matiere = matiereRepository.findById(dto.getMatiereId())
                .orElseThrow(() -> new RuntimeException("Matière non trouvée avec l'id: " + dto.getMatiereId()));
            Enseignant enseignant = enseignantRepository.findById(dto.getEnseignantId())
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'id: " + dto.getEnseignantId()));
            
            // Vérifier si l'enseignement existe déjà
            List<Enseignement> existing = enseignementRepository.findByClasseId(dto.getClasseId());
            existing = existing.stream()
                .filter(e -> e.getMatiere().getId().equals(dto.getMatiereId()))
                .filter(e -> e.getEnseignant().getId().equals(dto.getEnseignantId()))
                .collect(Collectors.toList());
            
            if (!existing.isEmpty()) {
                throw new RuntimeException("Cet enseignement existe déjà pour cet enseignant, classe et matière");
            }
            
            // Créer l'enseignement
            Enseignement enseignement = new Enseignement();
            enseignement.setClasse(classe);
            enseignement.setMatiere(matiere);
            enseignement.setEnseignant(enseignant);
            enseignement.setHeuresParSemaine(dto.getHeuresParSemaine());
            
            Enseignement saved = enseignementRepository.save(enseignement);
            return convertToDTO(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'attribution de l'enseignement: " + e.getMessage());
        }
    }
    
    public List<EnseignementDTO> getAllEnseignements() {
        try {
            return enseignementRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<EnseignementDTO> getEnseignementsParEnseignant(String enseignantId) {
        try {
            return enseignementRepository.findByEnseignantId(enseignantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<EnseignementDTO> getEnseignementsParClasse(String classeId) {
        try {
            return enseignementRepository.findByClasseId(classeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<EnseignementDTO> getEnseignementsParMatiere(String matiereId) {
        try {
            return enseignementRepository.findByMatiereId(matiereId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public EnseignementDTO updateEnseignement(EnseignementDTO dto) {
        try {
            Enseignement enseignement = enseignementRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Enseignement non trouvé avec l'id: " + dto.getId()));
            
            enseignement.setHeuresParSemaine(dto.getHeuresParSemaine());
            
            if (!enseignement.getEnseignant().getId().equals(dto.getEnseignantId())) {
                Enseignant nouvelEnseignant = enseignantRepository.findById(dto.getEnseignantId())
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'id: " + dto.getEnseignantId()));
                enseignement.setEnseignant(nouvelEnseignant);
            }
            
            if (!enseignement.getClasse().getId().equals(dto.getClasseId())) {
                Classe nouvelleClasse = classeRepository.findById(dto.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + dto.getClasseId()));
                enseignement.setClasse(nouvelleClasse);
            }
            
            if (!enseignement.getMatiere().getId().equals(dto.getMatiereId())) {
                Matiere nouvelleMatiere = matiereRepository.findById(dto.getMatiereId())
                    .orElseThrow(() -> new RuntimeException("Matière non trouvée avec l'id: " + dto.getMatiereId()));
                enseignement.setMatiere(nouvelleMatiere);
            }
            
            Enseignement saved = enseignementRepository.save(enseignement);
            return convertToDTO(saved);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la mise à jour de l'enseignement: " + e.getMessage());
        }
    }
    
    public void deleteEnseignement(String id) {
        try {
            enseignementRepository.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la suppression de l'enseignement: " + e.getMessage());
        }
    }
    
    // === MÉTHODES PRIVÉES ===
    
    private void genererCreneaux(HoraireJournalier horaire) {
        try {
            if (horaire.getEstJourCours() == null || !horaire.getEstJourCours() || 
                horaire.getHeureDebutMatin() == null || horaire.getHeureFinMatin() == null) {
                horaire.setCreneaux(new ArrayList<>());
                horaire.setNombreCreneaux(0);
                return;
            }
            
            List<String> creneaux = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            int dureeCreneau = horaire.getDureeCreneauMinutes() != null && horaire.getDureeCreneauMinutes() > 0 ? 
                              horaire.getDureeCreneauMinutes() : 60; // 60 min par défaut
            
            // Créneaux du matin
            LocalTime debut = LocalTime.parse(horaire.getHeureDebutMatin(), formatter);
            LocalTime fin = LocalTime.parse(horaire.getHeureFinMatin(), formatter);
            
            LocalTime current = debut;
            while (current.isBefore(fin)) {
                creneaux.add(current.format(formatter));
                current = current.plusMinutes(dureeCreneau);
            }
            
            // Créneaux de l'après-midi
            if (horaire.getHeureDebutApresMidi() != null && horaire.getHeureFinApresMidi() != null) {
                debut = LocalTime.parse(horaire.getHeureDebutApresMidi(), formatter);
                fin = LocalTime.parse(horaire.getHeureFinApresMidi(), formatter);
                
                current = debut;
                while (current.isBefore(fin)) {
                    creneaux.add(current.format(formatter));
                    current = current.plusMinutes(dureeCreneau);
                }
            }
            
            horaire.setCreneaux(creneaux);
            horaire.setNombreCreneaux(creneaux.size());
        } catch (Exception e) {
            e.printStackTrace();
            horaire.setCreneaux(new ArrayList<>());
            horaire.setNombreCreneaux(0);
        }
    }
    
    // Initialiser la configuration par défaut
    public void initialiserConfigurationParDefaut() {
        try {
            // Jours de cours standards
            String[] jours = {"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI"};
            
            for (String jour : jours) {
                Optional<HoraireJournalier> existing = horaireJournalierRepository.findByJourSemaine(jour);
                if (existing.isPresent()) {
                    continue; // Ne pas écraser les configurations existantes
                }
                
                HoraireJournalier horaire = new HoraireJournalier();
                horaire.setJourSemaine(jour);
                horaire.setEstJourCours(true);
                horaire.setDureeCreneauMinutes(60);
                
                if (jour.equals("LUNDI") || jour.equals("MARDI") || jour.equals("JEUDI")) {
                    // Journée complète
                    horaire.setHeureDebutMatin("08:00");
                    horaire.setHeureFinMatin("12:00");
                    horaire.setHeureDebutApresMidi("14:00");
                    horaire.setHeureFinApresMidi("18:00");
                } else if (jour.equals("MERCREDI")) {
                    // Demi-journée
                    horaire.setHeureDebutMatin("08:00");
                    horaire.setHeureFinMatin("12:00");
                    horaire.setHeureDebutApresMidi(null);
                    horaire.setHeureFinApresMidi(null);
                } else if (jour.equals("VENDREDI")) {
                    // Journée complète mais fin plus tôt
                    horaire.setHeureDebutMatin("08:00");
                    horaire.setHeureFinMatin("12:00");
                    horaire.setHeureDebutApresMidi("14:00");
                    horaire.setHeureFinApresMidi("16:00");
                }
                
                genererCreneaux(horaire);
                horaireJournalierRepository.save(horaire);
            }
            
            // SAMEDI pas de cours par défaut
            Optional<HoraireJournalier> samediExisting = horaireJournalierRepository.findByJourSemaine("SAMEDI");
            if (!samediExisting.isPresent()) {
                HoraireJournalier samedi = new HoraireJournalier();
                samedi.setJourSemaine("SAMEDI");
                samedi.setEstJourCours(false);
                horaireJournalierRepository.save(samedi);
            }
            
            // DIMANCHE pas de cours par défaut
            Optional<HoraireJournalier> dimancheExisting = horaireJournalierRepository.findByJourSemaine("DIMANCHE");
            if (!dimancheExisting.isPresent()) {
                HoraireJournalier dimanche = new HoraireJournalier();
                dimanche.setJourSemaine("DIMANCHE");
                dimanche.setEstJourCours(false);
                horaireJournalierRepository.save(dimanche);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'initialisation de la configuration par défaut: " + e.getMessage());
        }
    }
    
    // === MÉTHODES DE CONVERSION ===
    
    private EnseignementDTO convertToDTO(Enseignement enseignement) {
        try {
            EnseignementDTO dto = new EnseignementDTO();
            dto.setId(enseignement.getId());
            dto.setClasseId(enseignement.getClasse().getId());
            dto.setClasseNom(enseignement.getClasse().getNom());
            dto.setMatiereId(enseignement.getMatiere().getId());
            dto.setMatiereNom(enseignement.getMatiere().getNom());
            dto.setEnseignantId(enseignement.getEnseignant().getId());
            dto.setEnseignantNom(enseignement.getEnseignant().getNom() + " " + 
                                enseignement.getEnseignant().getPrenom());
            dto.setHeuresParSemaine(enseignement.getHeuresParSemaine());
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return new EnseignementDTO();
        }
    }
    
    private HoraireJournalierDTO convertHoraireToDTO(HoraireJournalier horaire) {
        try {
            HoraireJournalierDTO dto = new HoraireJournalierDTO();
            dto.setId(horaire.getId());
            dto.setJourSemaine(horaire.getJourSemaine());
            dto.setHeureDebutMatin(horaire.getHeureDebutMatin());
            dto.setHeureFinMatin(horaire.getHeureFinMatin());
            dto.setHeureDebutApresMidi(horaire.getHeureDebutApresMidi());
            dto.setHeureFinApresMidi(horaire.getHeureFinApresMidi());
            dto.setEstJourCours(horaire.getEstJourCours());
            dto.setDureeCreneauMinutes(horaire.getDureeCreneauMinutes());
            dto.setNombreCreneaux(horaire.getNombreCreneaux());
            dto.setCreneaux(horaire.getCreneaux() != null ? horaire.getCreneaux() : new ArrayList<>());
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return new HoraireJournalierDTO();
        }
    }
    
    private HoraireJournalier convertDTOToHoraire(HoraireJournalierDTO dto) {
        try {
            HoraireJournalier horaire = new HoraireJournalier();
            if (dto.getId() != null) {
                horaire.setId(dto.getId());
            }
            horaire.setJourSemaine(dto.getJourSemaine());
            horaire.setHeureDebutMatin(dto.getHeureDebutMatin());
            horaire.setHeureFinMatin(dto.getHeureFinMatin());
            horaire.setHeureDebutApresMidi(dto.getHeureDebutApresMidi());
            horaire.setHeureFinApresMidi(dto.getHeureFinApresMidi());
            horaire.setEstJourCours(dto.getEstJourCours());
            horaire.setDureeCreneauMinutes(dto.getDureeCreneauMinutes());
            return horaire;
        } catch (Exception e) {
            e.printStackTrace();
            return new HoraireJournalier();
        }
    }
}