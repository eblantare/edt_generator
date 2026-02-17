package com.edt.services;

import com.edt.dtos.EnseignantDTO;
import com.edt.dtos.MatiereDTO;
import com.edt.entities.Enseignant;
import com.edt.entities.Matiere;
import com.edt.repository.EnseignantRepository;
import com.edt.repository.MatiereRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class EnseignantService {
    
    @Autowired
    private EnseignantRepository enseignantRepository;
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    public Page<EnseignantDTO> getAllEnseignants(int page, int size, String search, 
                                                 String sortBy, String sortDirection) {
        
        System.out.println("📊 SERVICE - Paramètres reçus:");
        System.out.println("  page: " + page);
        System.out.println("  size: " + size);
        System.out.println("  search: " + search);
        System.out.println("  sortBy: " + sortBy);
        System.out.println("  sortDirection: " + sortDirection);
        
        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        
        Page<Enseignant> enseignantsPage;
        
        if (search != null && !search.trim().isEmpty()) {
            enseignantsPage = enseignantRepository.findBySearch(search.toLowerCase(), pageable);
        } else {
            enseignantsPage = enseignantRepository.findAll(pageable);
        }
        
        System.out.println("✅ Nombre d'enseignants trouvés: " + enseignantsPage.getContent().size());
        
        return enseignantsPage.map(this::convertToDTO);
    }
    
    public EnseignantDTO getEnseignantById(String id) {
        System.out.println("🔍 Recherche enseignant avec ID: " + id);
        
        Enseignant enseignant = enseignantRepository.findById(id)
            .orElseThrow(() -> {
                System.out.println("❌ Enseignant non trouvé avec ID: " + id);
                return new RuntimeException("Enseignant non trouvé");
            });
        
        System.out.println("✅ Enseignant trouvé: " + enseignant.getNom() + " " + enseignant.getPrenom());
        return convertToDTO(enseignant);
    }
    
    public EnseignantDTO createEnseignant(EnseignantDTO enseignantDTO) {
        System.out.println("➕ Création nouvel enseignant: " + enseignantDTO.getNom());
        System.out.println("📋 Données reçues:");
        System.out.println("  - Matière dominante ID: " + (enseignantDTO.getMatiereDominante() != null ? enseignantDTO.getMatiereDominante().getId() : "null"));
        System.out.println("  - Matière secondaire ID: " + (enseignantDTO.getMatiereSecondaire() != null ? enseignantDTO.getMatiereSecondaire().getId() : "null"));
        
        // Vérifier si le matricule existe déjà
        Enseignant existing = enseignantRepository.findByMatricule(enseignantDTO.getMatricule());
        if (existing != null) {
            System.out.println("❌ Matricule déjà utilisé: " + enseignantDTO.getMatricule());
            throw new RuntimeException("Un enseignant avec ce matricule existe déjà");
        }
        
        Enseignant enseignant = new Enseignant();
        enseignant.setNom(enseignantDTO.getNom());
        enseignant.setPrenom(enseignantDTO.getPrenom());
        enseignant.setMatricule(enseignantDTO.getMatricule());
        enseignant.setEmail(enseignantDTO.getEmail());
        enseignant.setTelephone(enseignantDTO.getTelephone());
        enseignant.setHeuresMaxHebdo(enseignantDTO.getHeuresMaxHebdo());
        
        // ⭐⭐ AJOUT CRITIQUE : GESTION DES MATIÈRES DIRECTES ⭐⭐
        if (enseignantDTO.getMatiereDominante() != null && enseignantDTO.getMatiereDominante().getId() != null) {
            Matiere matiereDom = matiereRepository.findById(enseignantDTO.getMatiereDominante().getId())
                .orElse(null);
            if (matiereDom != null) {
                enseignant.setMatiereDominante(matiereDom);
                System.out.println("✅ Matière dominante associée: " + matiereDom.getNom());
            } else {
                System.out.println("⚠️ Matière dominante non trouvée avec ID: " + enseignantDTO.getMatiereDominante().getId());
            }
        }
        
        if (enseignantDTO.getMatiereSecondaire() != null && enseignantDTO.getMatiereSecondaire().getId() != null) {
            Matiere matiereSec = matiereRepository.findById(enseignantDTO.getMatiereSecondaire().getId())
                .orElse(null);
            if (matiereSec != null) {
                enseignant.setMatiereSecondaire(matiereSec);
                System.out.println("✅ Matière secondaire associée: " + matiereSec.getNom());
            } else {
                System.out.println("⚠️ Matière secondaire non trouvée avec ID: " + enseignantDTO.getMatiereSecondaire().getId());
            }
        }
        
        enseignant = enseignantRepository.save(enseignant);
        System.out.println("✅ Enseignant créé avec ID: " + enseignant.getId());
        
        return convertToDTO(enseignant);
    }
    
    public EnseignantDTO updateEnseignant(String id, EnseignantDTO enseignantDTO) {
        System.out.println("✏️ Mise à jour enseignant ID: " + id);
        System.out.println("📋 Données reçues:");
        System.out.println("  - Matière dominante ID: " + (enseignantDTO.getMatiereDominante() != null ? enseignantDTO.getMatiereDominante().getId() : "null"));
        System.out.println("  - Matière secondaire ID: " + (enseignantDTO.getMatiereSecondaire() != null ? enseignantDTO.getMatiereSecondaire().getId() : "null"));
        
        Enseignant enseignant = enseignantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        
        // Vérifier si le nouveau matricule est utilisé par un autre enseignant
        if (!enseignant.getMatricule().equals(enseignantDTO.getMatricule())) {
            Enseignant existing = enseignantRepository.findByMatricule(enseignantDTO.getMatricule());
            if (existing != null && !existing.getId().equals(id)) {
                System.out.println("❌ Matricule déjà utilisé par un autre enseignant: " + enseignantDTO.getMatricule());
                throw new RuntimeException("Ce matricule est déjà utilisé par un autre enseignant");
            }
        }
        
        enseignant.setNom(enseignantDTO.getNom());
        enseignant.setPrenom(enseignantDTO.getPrenom());
        enseignant.setMatricule(enseignantDTO.getMatricule());
        enseignant.setEmail(enseignantDTO.getEmail());
        enseignant.setTelephone(enseignantDTO.getTelephone());
        enseignant.setHeuresMaxHebdo(enseignantDTO.getHeuresMaxHebdo());
        
        // ⭐⭐ AJOUT CRITIQUE : GESTION DES MATIÈRES DIRECTES ⭐⭐
        // Matière dominante
        if (enseignantDTO.getMatiereDominante() != null && enseignantDTO.getMatiereDominante().getId() != null) {
            Matiere matiereDom = matiereRepository.findById(enseignantDTO.getMatiereDominante().getId())
                .orElse(null);
            enseignant.setMatiereDominante(matiereDom);
            System.out.println("✅ Matière dominante mise à jour: " + (matiereDom != null ? matiereDom.getNom() : "null"));
        } else {
            enseignant.setMatiereDominante(null);
            System.out.println("✅ Matière dominante effacée");
        }
        
        // Matière secondaire
        if (enseignantDTO.getMatiereSecondaire() != null && enseignantDTO.getMatiereSecondaire().getId() != null) {
            Matiere matiereSec = matiereRepository.findById(enseignantDTO.getMatiereSecondaire().getId())
                .orElse(null);
            enseignant.setMatiereSecondaire(matiereSec);
            System.out.println("✅ Matière secondaire mise à jour: " + (matiereSec != null ? matiereSec.getNom() : "null"));
        } else {
            enseignant.setMatiereSecondaire(null);
            System.out.println("✅ Matière secondaire effacée");
        }
        
        enseignant = enseignantRepository.save(enseignant);
        System.out.println("✅ Enseignant mis à jour: " + enseignant.getNom());
        
        return convertToDTO(enseignant);
    }
    
    public void deleteEnseignant(String id) {
        System.out.println("🗑️ Suppression enseignant ID: " + id);
        enseignantRepository.deleteById(id);
        System.out.println("✅ Enseignant supprimé");
    }
    
    private EnseignantDTO convertToDTO(Enseignant enseignant) {
        System.out.println("🔄 Conversion Enseignant -> DTO: " + enseignant.getNom());
        
        EnseignantDTO dto = new EnseignantDTO();
        dto.setId(enseignant.getId());
        dto.setNom(enseignant.getNom());
        dto.setPrenom(enseignant.getPrenom());
        dto.setMatricule(enseignant.getMatricule());
        dto.setEmail(enseignant.getEmail());
        dto.setTelephone(enseignant.getTelephone());
        dto.setHeuresMaxHebdo(enseignant.getHeuresMaxHebdo());
        
        // ⭐⭐ AJOUT CRITIQUE : CONVERSION DES MATIÈRES DIRECTES ⭐⭐
        if (enseignant.getMatiereDominante() != null) {
            dto.setMatiereDominante(convertMatiereToDTO(enseignant.getMatiereDominante()));
            System.out.println("⭐ Matière dominante: " + enseignant.getMatiereDominante().getNom());
        } else {
            System.out.println("ℹ️ Pas de matière dominante définie");
        }
        
        if (enseignant.getMatiereSecondaire() != null) {
            dto.setMatiereSecondaire(convertMatiereToDTO(enseignant.getMatiereSecondaire()));
            System.out.println("📘 Matière secondaire: " + enseignant.getMatiereSecondaire().getNom());
        } else {
            System.out.println("ℹ️ Pas de matière secondaire définie");
        }
        
        return dto;
    }
    
    private MatiereDTO convertMatiereToDTO(Matiere matiere) {
        if (matiere == null) return null;
        
        MatiereDTO dto = new MatiereDTO();
        dto.setId(matiere.getId());
        dto.setCode(matiere.getCode());
        dto.setNom(matiere.getNom());
        dto.setCycle(matiere.getCycle());
        dto.setNiveauClasse(matiere.getNiveauClasse());
        
        return dto;
    }
}