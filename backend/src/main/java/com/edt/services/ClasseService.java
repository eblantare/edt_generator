// C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\ClasseService.java
package com.edt.services;

import com.edt.dtos.ClasseDTO;
import com.edt.entities.Classe;
import com.edt.repository.ClasseRepository;
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
public class ClasseService {
    
    @Autowired
    private ClasseRepository classeRepository;
    
    // Méthode existante pour compatibilité
    public List<ClasseDTO> getAllClasses() {
        return classeRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    // NOUVELLE méthode pour la pagination
    public Page<ClasseDTO> getAllClassesPaginated(
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
        
        Page<Classe> classesPage;
        
        if (search != null && !search.trim().isEmpty()) {
            // Recherche avec pagination
            classesPage = classeRepository.searchClasses(search, pageable);
        } else {
            // Toutes les classes avec pagination
            classesPage = classeRepository.findAll(pageable);
        }
        
        // Convertir Page<Classe> en Page<ClasseDTO>
        return classesPage.map(this::convertToDTO);
    }
    
    public ClasseDTO getClasseById(String id) {
        Classe classe = classeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        return convertToDTO(classe);
    }
    
    public ClasseDTO createClasse(ClasseDTO classeDTO) {
        // Vérifier si le nom existe déjà
        Classe existing = classeRepository.findByNom(classeDTO.getNom());
        if (existing != null) {
            throw new RuntimeException("Une classe avec ce nom existe déjà");
        }
        
        Classe classe = new Classe();
        classe.setNom(classeDTO.getNom());
        classe.setNiveau(classeDTO.getNiveau());
        classe.setFiliere(classeDTO.getFiliere());
        classe.setEffectif(classeDTO.getEffectif());
        
        classe = classeRepository.save(classe);
        return convertToDTO(classe);
    }
    
    public ClasseDTO updateClasse(String id, ClasseDTO classeDTO) {
        Classe classe = classeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        
        // Vérifier si le nouveau nom est utilisé par une autre classe
        if (!classe.getNom().equals(classeDTO.getNom())) {
            Classe existing = classeRepository.findByNom(classeDTO.getNom());
            if (existing != null && !existing.getId().equals(id)) {
                throw new RuntimeException("Ce nom est déjà utilisé par une autre classe");
            }
        }
        
        classe.setNom(classeDTO.getNom());
        classe.setNiveau(classeDTO.getNiveau());
        classe.setFiliere(classeDTO.getFiliere());
        classe.setEffectif(classeDTO.getEffectif());
        
        classe = classeRepository.save(classe);
        return convertToDTO(classe);
    }
    
    public void deleteClasse(String id) {
        classeRepository.deleteById(id);
    }
    
    private ClasseDTO convertToDTO(Classe classe) {
        ClasseDTO dto = new ClasseDTO();
        dto.setId(classe.getId());
        dto.setNom(classe.getNom());
        dto.setNiveau(classe.getNiveau());
        dto.setFiliere(classe.getFiliere());
        dto.setEffectif(classe.getEffectif());
        return dto;
    }
}