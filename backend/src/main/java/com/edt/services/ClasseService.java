// C:\projets\java\edt-generator\backend\src\main\java\com\edt\services\ClasseService.java
package com.edt.services;

import com.edt.dtos.ClasseDTO;
import com.edt.entities.Classe;
import com.edt.entities.EmploiDuTemps;
import com.edt.repository.ClasseRepository;
import com.edt.repository.EmploiDuTempsRepository;
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
    
    @Autowired
    private EmploiDuTempsRepository emploiDuTempsRepository;
    
    public List<ClasseDTO> getAllClasses() {
        return classeRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public Page<ClasseDTO> getAllClassesPaginated(
        int page, 
        int size, 
        String search,
        String sortBy,
        String sortDirection
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Classe> classesPage;
        
        if (search != null && !search.trim().isEmpty()) {
            classesPage = classeRepository.searchClasses(search, pageable);
        } else {
            classesPage = classeRepository.findAll(pageable);
        }
        
        return classesPage.map(this::convertToDTO);
    }
    
    public ClasseDTO getClasseById(String id) {
        Classe classe = classeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        return convertToDTO(classe);
    }
    
    public ClasseDTO createClasse(ClasseDTO classeDTO) {
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
    
    @Transactional
    public void deleteClasse(String id) {
        System.out.println("🗑️ Tentative de suppression de la classe: " + id);
        
        // Vérifier si la classe existe
        Classe classe = classeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        
        // Vérifier si la classe est utilisée dans des emplois du temps
        List<EmploiDuTemps> emplois = emploiDuTempsRepository.findByClasseId(id);
        
        if (!emplois.isEmpty()) {
            // Construire un message clair pour l'utilisateur
            String message = String.format(
                "Impossible de supprimer la classe '%s' car elle est utilisée dans %d emploi(s) du temps. " +
                "Veuillez d'abord supprimer les emplois du temps associés.",
                classe.getNom(),
                emplois.size()
            );
            System.out.println("❌ " + message);
            throw new RuntimeException(message);
        }
        
        // Si aucune dépendance, supprimer la classe
        classeRepository.delete(classe);
        System.out.println("✅ Classe supprimée avec succès: " + id);
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