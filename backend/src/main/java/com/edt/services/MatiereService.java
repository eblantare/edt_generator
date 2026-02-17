package com.edt.services;

import com.edt.dtos.MatiereDTO;
import com.edt.entities.Matiere;
import com.edt.repository.MatiereRepository;
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
public class MatiereService {
    
    @Autowired
    private MatiereRepository matiereRepository;
    
    // Méthode existante pour compatibilité
    public List<MatiereDTO> getAllMatieres() {
        return matiereRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // NOUVELLE méthode pour la pagination
    public Page<MatiereDTO> getAllMatieresPaginated(
        int page, 
        int size, 
        String search,
        String sortBy,
        String sortDirection
    ) {
        // Créer l'objet Pageable avec tri
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Matiere> matieresPage;
        
        if (search != null && !search.trim().isEmpty()) {
            // Recherche avec pagination
            matieresPage = matiereRepository.searchMatieres(search, pageable);
        } else {
            // Toutes les matières avec pagination
            matieresPage = matiereRepository.findAll(pageable);
        }
        
        // Convertir Page<Matiere> en Page<MatiereDTO>
        return matieresPage.map(this::convertToDTO);
    }
    
    public MatiereDTO getMatiereById(String id) {
        Matiere matiere = matiereRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Matière non trouvée"));
        return convertToDTO(matiere);
    }
    
    public MatiereDTO createMatiere(MatiereDTO matiereDTO) {
        // Vérifier si le code existe déjà
        Matiere existing = matiereRepository.findByCode(matiereDTO.getCode());
        if (existing != null) {
            throw new RuntimeException("Une matière avec ce code existe déjà");
        }
        
        Matiere matiere = new Matiere();
        matiere.setCode(matiereDTO.getCode());
        matiere.setNom(matiereDTO.getNom());
        matiere.setCycle(matiereDTO.getCycle());
        matiere.setNiveauClasse(matiereDTO.getNiveauClasse());
        
        matiere = matiereRepository.save(matiere);
        return convertToDTO(matiere);
    }
    
    public MatiereDTO updateMatiere(String id, MatiereDTO matiereDTO) {
        Matiere matiere = matiereRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Matière non trouvée"));
        
        // Vérifier si le nouveau code est utilisé par une autre matière
        if (!matiere.getCode().equals(matiereDTO.getCode())) {
            Matiere existing = matiereRepository.findByCode(matiereDTO.getCode());
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("Ce code est déjà utilisé par une autre matière");
            }
        }
        
        matiere.setCode(matiereDTO.getCode());
        matiere.setNom(matiereDTO.getNom());
        matiere.setCycle(matiereDTO.getCycle());
        matiere.setNiveauClasse(matiereDTO.getNiveauClasse());
        
        matiere = matiereRepository.save(matiere);
        return convertToDTO(matiere);
    }
    
    public void deleteMatiere(String id) {
        matiereRepository.deleteById(id);
    }
    
    private MatiereDTO convertToDTO(Matiere matiere) {
        MatiereDTO dto = new MatiereDTO();
        dto.setId(matiere.getId());
        dto.setCode(matiere.getCode());
        dto.setNom(matiere.getNom());
        dto.setCycle(matiere.getCycle());
        dto.setNiveauClasse(matiere.getNiveauClasse());
        return dto;
    }
    
    // Méthode utilitaire pour le mapping des cycles (optionnel)
    public String getCycleDisplayName(String cycleCode) {
        switch (cycleCode) {
            case "college": return "Collège";
            case "lycee": return "Lycée Général";
            case "lycee_tech": return "Lycée Technique";
            case "lycee_pro": return "Lycée Professionnel";
            case "bt": return "Brevet de Technicien";
            default: return cycleCode;
        }
    }
}