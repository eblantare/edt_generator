package com.edt.controllers;

import com.edt.dtos.EcoleDTO;
import com.edt.entities.Ecole;
import com.edt.repository.EcoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ecole")
@CrossOrigin(origins = "http://localhost:4200")
public class EcoleController {

    @Autowired
    private EcoleRepository ecoleRepository;

    @GetMapping
    public ResponseEntity<EcoleDTO> getEcole() {
        return ecoleRepository.findFirstByOrderByCreatedAtAsc()
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EcoleDTO> createOrUpdateEcole(@RequestBody EcoleDTO ecoleDTO) {
        Ecole ecole = ecoleRepository.findFirstByOrderByCreatedAtAsc()
                .orElse(new Ecole());
        
        ecole.setNom(ecoleDTO.getNom());
        ecole.setTelephone(ecoleDTO.getTelephone());
        ecole.setAdresse(ecoleDTO.getAdresse());
        ecole.setLogo(ecoleDTO.getLogo());
        ecole.setDevise(ecoleDTO.getDevise());
        ecole.setDre(ecoleDTO.getDre());
        ecole.setIesg(ecoleDTO.getIesg());
        ecole.setBp(ecoleDTO.getBp());
        
        ecole = ecoleRepository.save(ecole);
        return ResponseEntity.ok(convertToDTO(ecole));
    }

    @GetMapping("/all")
    public ResponseEntity<List<EcoleDTO>> getAllEcoles() {
        List<EcoleDTO> ecoles = ecoleRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ecoles);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEcole(@PathVariable String id) {
       try {
        ecoleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "École supprimée avec succès"));
       } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
       }
    }

    private EcoleDTO convertToDTO(Ecole ecole) {
        EcoleDTO dto = new EcoleDTO();
        dto.setId(ecole.getId());
        dto.setNom(ecole.getNom());
        dto.setTelephone(ecole.getTelephone());
        dto.setAdresse(ecole.getAdresse());
        dto.setLogo(ecole.getLogo());
        dto.setDevise(ecole.getDevise());
        dto.setDre(ecole.getDre());
        dto.setIesg(ecole.getIesg());
        dto.setBp(ecole.getBp());
        return dto;
    }
}
