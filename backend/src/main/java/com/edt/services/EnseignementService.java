// C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\EnseignementService.java
package com.edt.services;

import com.edt.dtos.EnseignementDTO;
import com.edt.entities.*;
import com.edt.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnseignementService {
    
    @Autowired
    private EnseignementRepository enseignementRepository;
    
    @Autowired
    private EnseignantRepository enseignantRepository;
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    @Autowired
    private ClasseRepository classeRepository;
    
    // === CRUD OPERATIONS ===
    
    public EnseignementDTO createEnseignement(EnseignementDTO dto) {
        try {
            // Validation
            if (dto.getHeuresParSemaine() == null || dto.getHeuresParSemaine() <= 0) {
                throw new RuntimeException("Le nombre d'heures par semaine doit être positif");
            }
            
            // Vérifier si l'enseignement existe déjà
            List<Enseignement> existing = enseignementRepository.findByEnseignantIdAndClasseIdAndMatiereId(
                dto.getEnseignantId(), dto.getClasseId(), dto.getMatiereId());
            
            if (!existing.isEmpty()) {
                throw new RuntimeException("Cet enseignement existe déjà");
            }
            
            // Récupérer les entités
            Enseignant enseignant = enseignantRepository.findById(dto.getEnseignantId())
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
            
            Matiere matiere = matiereRepository.findById(dto.getMatiereId())
                .orElseThrow(() -> new RuntimeException("Matière non trouvée"));
            
            Classe classe = classeRepository.findById(dto.getClasseId())
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
            
            // Créer l'enseignement
            Enseignement enseignement = new Enseignement();
            enseignement.setEnseignant(enseignant);
            enseignement.setMatiere(matiere);
            enseignement.setClasse(classe);
            enseignement.setHeuresParSemaine(dto.getHeuresParSemaine());
            
            Enseignement saved = enseignementRepository.save(enseignement);
            return convertToDTO(saved);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la création de l'enseignement: " + e.getMessage());
        }
    }
    
    public EnseignementDTO getEnseignementById(String id) {
        return enseignementRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new RuntimeException("Enseignement non trouvé"));
    }
    
    public EnseignementDTO updateEnseignement(EnseignementDTO dto) {
        try {
            Enseignement enseignement = enseignementRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Enseignement non trouvé"));
            
            // Mettre à jour les heures
            enseignement.setHeuresParSemaine(dto.getHeuresParSemaine());
            
            // Mettre à jour l'enseignant si nécessaire
            if (!enseignement.getEnseignant().getId().equals(dto.getEnseignantId())) {
                Enseignant nouvelEnseignant = enseignantRepository.findById(dto.getEnseignantId())
                    .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
                enseignement.setEnseignant(nouvelEnseignant);
            }
            
            // Mettre à jour la matière si nécessaire
            if (!enseignement.getMatiere().getId().equals(dto.getMatiereId())) {
                Matiere nouvelleMatiere = matiereRepository.findById(dto.getMatiereId())
                    .orElseThrow(() -> new RuntimeException("Matière non trouvée"));
                enseignement.setMatiere(nouvelleMatiere);
            }
            
            // Mettre à jour la classe si nécessaire
            if (!enseignement.getClasse().getId().equals(dto.getClasseId())) {
                Classe nouvelleClasse = classeRepository.findById(dto.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
                enseignement.setClasse(nouvelleClasse);
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
    
    // === LIST OPERATIONS ===
    
    public Page<EnseignementDTO> getAllEnseignements(int page, int size, String search, String sortBy, String sortDirection) {
        try {
            System.out.println("📊 SERVICE - Paramètres reçus:");
            System.out.println("  page: " + page);
            System.out.println("  size: " + size);
            System.out.println("  search: " + search);
            System.out.println("  sortBy: " + sortBy);
            System.out.println("  sortDirection: " + sortDirection);
            
            // Créer le tri
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            System.out.println("🔍 Pageable créé: " + pageable);
            
            // Récupérer les données
            Page<Enseignement> enseignements;
            
            if (search != null && !search.trim().isEmpty()) {
                // Recherche par nom d'enseignant, matière ou classe
                enseignements = enseignementRepository.searchEnseignements(search.toLowerCase(), pageable);
            } else {
                enseignements = enseignementRepository.findAll(pageable);
            }
            
            System.out.println("✅ Nombre d'enseignements trouvés: " + enseignements.getNumberOfElements());
            
            // Convertir en DTO
            Page<EnseignementDTO> dtoPage = enseignements.map(this::convertToDTO);
            
            return dtoPage;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur dans getAllEnseignements: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des enseignements: " + e.getMessage());
        }
    }
    
    public List<EnseignementDTO> getEnseignementsByEnseignant(String enseignantId) {
        try {
            return enseignementRepository.findByEnseignantId(enseignantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des enseignements par enseignant: " + e.getMessage());
        }
    }
    
    public List<EnseignementDTO> getEnseignementsByClasse(String classeId) {
        try {
            return enseignementRepository.findByClasseId(classeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des enseignements par classe: " + e.getMessage());
        }
    }
    
    public List<EnseignementDTO> getEnseignementsByMatiere(String matiereId) {
        try {
            return enseignementRepository.findByMatiereId(matiereId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de la récupération des enseignements par matière: " + e.getMessage());
        }
    }
    
    // === STATISTICS ===
    
    public Integer getTotalHeuresByEnseignant(String enseignantId) {
        try {
            List<Enseignement> enseignements = enseignementRepository.findByEnseignantId(enseignantId);
            return enseignements.stream()
                .mapToInt(Enseignement::getHeuresParSemaine)
                .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public Integer getTotalHeuresByClasse(String classeId) {
        try {
            List<Enseignement> enseignements = enseignementRepository.findByClasseId(classeId);
            return enseignements.stream()
                .mapToInt(Enseignement::getHeuresParSemaine)
                .sum();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // === CONVERSION ===
    
    private EnseignementDTO convertToDTO(Enseignement enseignement) {
        try {
            System.out.println("🔄 Conversion Enseignement -> DTO");
            
            EnseignementDTO dto = new EnseignementDTO();
            dto.setId(enseignement.getId());
            dto.setHeuresParSemaine(enseignement.getHeuresParSemaine());
            
            // Enseignant
            if (enseignement.getEnseignant() != null) {
                dto.setEnseignantId(enseignement.getEnseignant().getId());
                dto.setEnseignantNom(enseignement.getEnseignant().getNom());
                dto.setEnseignantPrenom(enseignement.getEnseignant().getPrenom());
                System.out.println("  Enseignant: " + dto.getEnseignantNom() + " " + dto.getEnseignantPrenom());
            }
            
            // Matière
            if (enseignement.getMatiere() != null) {
                dto.setMatiereId(enseignement.getMatiere().getId());
                dto.setMatiereCode(enseignement.getMatiere().getCode());
                dto.setMatiereNom(enseignement.getMatiere().getNom());
                System.out.println("  Matière: " + dto.getMatiereCode() + " - " + dto.getMatiereNom());
            }
            
            // Classe
            if (enseignement.getClasse() != null) {
                dto.setClasseId(enseignement.getClasse().getId());
                dto.setClasseNom(enseignement.getClasse().getNom());
                System.out.println("  Classe: " + dto.getClasseNom());
            }
            
            return dto;
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la conversion: " + e.getMessage());
            e.printStackTrace();
            return new EnseignementDTO();
        }
    }
    
    // Méthode pour vérifier si un enseignement existe déjà
    private List<Enseignement> findByEnseignantIdAndClasseIdAndMatiereId(String enseignantId, String classeId, String matiereId) {
        return enseignementRepository.findByEnseignantIdAndClasseId(enseignantId, classeId)
            .stream()
            .filter(e -> e.getMatiere().getId().equals(matiereId))
            .collect(Collectors.toList());
    }
}