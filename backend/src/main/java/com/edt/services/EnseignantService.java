package com.edt.services;

import com.edt.dtos.EnseignantDTO;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class EnseignantService {
    
    @Autowired
    private EnseignantRepository enseignantRepository;
    
    @Autowired
    private EnseignementRepository enseignementRepository;
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    // === CRUD OPERATIONS ===
    
    public EnseignantDTO createEnseignant(EnseignantDTO dto) {
        // Vérifier si le matricule existe déjà
        Optional<Enseignant> existing = enseignantRepository.findByMatricule(dto.getMatricule());
        if (existing.isPresent()) {
            throw new RuntimeException("Un enseignant avec ce matricule existe déjà");
        }
        
        Enseignant enseignant = new Enseignant();
        enseignant.setMatricule(dto.getMatricule());
        enseignant.setNom(dto.getNom());
        enseignant.setPrenom(dto.getPrenom());
        enseignant.setTelephone(dto.getTelephone());
        enseignant.setEmail(dto.getEmail());
        enseignant.setHeuresMaxHebdo(dto.getHeuresMaxHebdo() != null ? dto.getHeuresMaxHebdo() : 24);
        
        // ✅ Gestion des matières
        if (dto.getMatiereDominanteId() != null && !dto.getMatiereDominanteId().isEmpty()) {
            matiereRepository.findById(dto.getMatiereDominanteId())
                .ifPresent(enseignant::setMatiereDominante);
        }
        
        if (dto.getMatiereSecondaireId() != null && !dto.getMatiereSecondaireId().isEmpty()) {
            matiereRepository.findById(dto.getMatiereSecondaireId())
                .ifPresent(enseignant::setMatiereSecondaire);
        }
        
        Enseignant saved = enseignantRepository.save(enseignant);
        return convertToDTO(saved);
    }
    
    public EnseignantDTO updateEnseignant(String id, EnseignantDTO dto) {
        Enseignant enseignant = enseignantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
        
        enseignant.setNom(dto.getNom());
        enseignant.setPrenom(dto.getPrenom());
        enseignant.setTelephone(dto.getTelephone());
        enseignant.setEmail(dto.getEmail());
        enseignant.setHeuresMaxHebdo(dto.getHeuresMaxHebdo());
        
        // ✅ Mise à jour des matières
        if (dto.getMatiereDominanteId() != null && !dto.getMatiereDominanteId().isEmpty()) {
            matiereRepository.findById(dto.getMatiereDominanteId())
                .ifPresent(enseignant::setMatiereDominante);
        } else {
            enseignant.setMatiereDominante(null);
        }
        
        if (dto.getMatiereSecondaireId() != null && !dto.getMatiereSecondaireId().isEmpty()) {
            matiereRepository.findById(dto.getMatiereSecondaireId())
                .ifPresent(enseignant::setMatiereSecondaire);
        } else {
            enseignant.setMatiereSecondaire(null);
        }
        
        Enseignant saved = enseignantRepository.save(enseignant);
        return convertToDTO(saved);
    }
    
    public void deleteEnseignant(String id) {
        enseignantRepository.deleteById(id);
    }
    
    public EnseignantDTO getEnseignantById(String id) {
        return enseignantRepository.findById(id)
            .map(this::convertToDTO)
            .orElseThrow(() -> new RuntimeException("Enseignant non trouvé"));
    }
    
    // === LIST OPERATIONS ===
    
    public Page<EnseignantDTO> getAllEnseignants(int page, int size, String search, String sortBy, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Enseignant> enseignants;
        if (search != null && !search.trim().isEmpty()) {
            enseignants = enseignantRepository.findBySearch(search.toLowerCase(), pageable);
        } else {
            enseignants = enseignantRepository.findAll(pageable);
        }
        
        return enseignants.map(this::convertToDTO);
    }
    
    public List<EnseignantDTO> getAllEnseignantsSimple() {
        return enseignantRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // === STATISTIQUES ===
    
    public Integer getTotalHeuresParSemaine(String enseignantId) {
        List<Enseignement> enseignements = enseignementRepository.findByEnseignantId(enseignantId);
        return enseignements.stream()
            .mapToInt(Enseignement::getHeuresParSemaine)
            .sum();
    }
    
    // === CONVERSION ===
    
    private EnseignantDTO convertToDTO(Enseignant enseignant) {
        EnseignantDTO dto = new EnseignantDTO();
        dto.setId(enseignant.getId());
        dto.setMatricule(enseignant.getMatricule());
        dto.setNom(enseignant.getNom());
        dto.setPrenom(enseignant.getPrenom());
        dto.setTelephone(enseignant.getTelephone());
        dto.setEmail(enseignant.getEmail());
        dto.setHeuresMaxHebdo(enseignant.getHeuresMaxHebdo());
        
        // ✅ Ajouter les matières au DTO
        if (enseignant.getMatiereDominante() != null) {
            dto.setMatiereDominante(enseignant.getMatiereDominante());
            dto.setMatiereDominanteId(enseignant.getMatiereDominante().getId());
        }
        
        if (enseignant.getMatiereSecondaire() != null) {
            dto.setMatiereSecondaire(enseignant.getMatiereSecondaire());
            dto.setMatiereSecondaireId(enseignant.getMatiereSecondaire().getId());
        }
        
        return dto;
    }
}